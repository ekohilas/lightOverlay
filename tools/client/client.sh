#!/bin/bash
# Drive a real Necesse client headlessly: Xvfb + Mesa software OpenGL + the non-Steam StartDesktopClient
# from the dedicated server's Server.jar. Needs res.data (game textures/sounds/fonts, from the user's Steam
# install) in the server dir; the cloud environment's setup script puts it there. See CLAUDE.md.
#
#   client.sh start [world]   build/jar mod, enter <world> (default modtest) with a character; ~3-4 min
#   client.sh shot <png>      screenshot the 1280x720 screen
#   client.sh key <keys...>   xdotool key names, e.g. F8, ctrl+z (each held 0.3s, see note below)
#   client.sh chat <text>     send a chat line, e.g. "/lightoverlayalpha 0.5"
#   client.sh click <x> <y>   left click at screen coordinates
#   client.sh log [n]         last n relevant log lines (default 20)
#   client.sh stop
#
# Note: `xdotool key` presses and releases within one frame; a Control whose activate() checks isPressed()
# then sees both events as pressed and toggles twice. `key` here holds each key like a person would.
set -u
REPO=$(cd "$(dirname "$0")/../.." && pwd)
SRV=$(ls -d "$HOME"/.local/share/Steam/steamapps/common/Necesse/necesse-server-*/ 2>/dev/null | tail -1)
OUT="$REPO/build/client"
LOG="$OUT/client.log"
GAMELOG="$HOME/.config/Necesse/latest-log.txt"
export DISPLAY=:99

die() { echo "client.sh: $*" >&2; exit 1; }
wait_log() { # <pattern> <timeout s> [file]
  local f=${3:-$LOG} t=0
  until grep -q "$1" "$f" 2>/dev/null; do
    sleep 2; t=$((t + 2))
    if grep -q "Crash outside ticking\|Printing crash log" "$LOG" 2>/dev/null || ! pgrep -f '^[^ ]*java .*StartDesktopClient' >/dev/null; then
      grep -vE "JAVA_TOOL|ALSA|OpenAL" "$LOG" | tail -30; die "client crashed or exited"
    fi
    [ $t -ge "$2" ] && die "timed out waiting for: $1"
  done
}
win() { xdotool search --name Necesse 2>/dev/null | head -1; }

cmd=${1:-}; shift || true
case "$cmd" in
start)
  WORLD=${1:-modtest}
  [ -n "$SRV" ] || die "no dedicated server install found"
  [ -f "$SRV/res.data" ] || die "missing $SRV/res.data (game assets); see CLAUDE.md"
  command -v xdotool >/dev/null || die "xdotool not installed (apt-get install -y xdotool)"
  ls "$REPO"/build/jar/*.jar >/dev/null 2>&1 || die "no mod jar; run ./gradlew buildModJar"
  mkdir -p "$OUT"
  pgrep -f '^[^ ]*java .*StartDesktopClient' >/dev/null && die "client already running (client.sh stop)"
  if ! xdotool getdisplaygeometry >/dev/null 2>&1; then
    pkill -x Xvfb; rm -f /tmp/.X99-lock /tmp/.X11-unix/X99   # stale lock makes a new Xvfb exit
    setsid Xvfb :99 -screen 0 1280x720x24 >"$OUT/xvfb.log" 2>&1 < /dev/null &
    for _ in $(seq 20); do xdotool getdisplaygeometry >/dev/null 2>&1 && break; sleep 0.5; done
    xdotool getdisplaygeometry >/dev/null 2>&1 || die "Xvfb did not start (see $OUT/xvfb.log)"
  fi
  # The desktop client's auth number is a random UUID's most significant bits (saved in cfg/auth), and
  # ReportUtils.getSessionID crashes with Random.nextInt((int) auth) when the low 32 bits are <= 0: about half of
  # fresh installs. Pin a serialized UUID whose low bits are positive.
  AUTH="$HOME/.config/Necesse/cfg/auth"
  if [ ! -f "$AUTH" ]; then
    mkdir -p "$(dirname "$AUTH")"
    python3 -I -c 'import sys
h=bytes.fromhex("aced00057372000e6a6176612e7574696c2e55554944bc9903f7986d852f0200024a000c6c65617374536967426974734a000b6d6f7374536967426974737870")
open(sys.argv[1],"wb").write(h+(0x6c69676874746573).to_bytes(8,"big")+(0x4c4f54455354AB01).to_bytes(8,"big"))' "$AUTH"
  fi
  cd "$SRV" || exit 1
  if [ ! -f "$HOME/.config/Necesse/saves/worlds/$WORLD.zip" ]; then
    echo "creating world $WORLD with the mod (dedicated server)..."
    (sleep 40; echo stop) | timeout 150 ./jre/bin/java -Xmx3G -jar Server.jar -nogui \
      -mod "$REPO/build/jar" -world "$WORLD" > "$OUT/server.log" 2>&1
  fi
  echo "starting client (log: $LOG)..."
  LIBGL_ALWAYS_SOFTWARE=1 ALSOFT_DRIVERS=null nohup ./jre/bin/java -Xmx4G -cp "Server.jar:lib/*" \
    StartDesktopClient -mod "$REPO/build/jar" -windowsize 1280,720 -load "$WORLD" > "$LOG" 2>&1 &
  wait_log "Started singleplayer server" 300
  if grep -q "Creating new player" "$LOG"; then
    sleep 10
    xdotool mousemove 640 535 click 1; sleep 10          # Create new character
    xdotool mousemove 640 547 click 1; sleep 2            # Player name field
    xdotool type --delay 150 Tester; sleep 3
    xdotool mousemove 540 587 click 1; sleep 15           # Create
  else
    sleep 15
  fi
  xdotool mousemove 811 184 click 1                       # Play on the first character
  wait_log "has joined!" 180 "$GAMELOG"
  sleep 20
  grep -q "Crash outside ticking\|Printing crash log" "$LOG" && { grep -m1 -A3 "Exceptions:\|crash" "$LOG"; die "client crashed after joining"; }
  echo "in world $WORLD"
  ;;
shot)  import -window root "${1:?png path}" ;;
key)   W=$(win); for k in "$@"; do xdotool keydown --window "$W" "$k"; sleep 0.3; xdotool keyup --window "$W" "$k"; sleep 1; done ;;
chat)  W=$(win); xdotool key --window "$W" Return; sleep 2; xdotool type --delay 100 "$*"; sleep 1; xdotool key Return ;;
click) xdotool mousemove "$1" "$2" click 1 ;;
log)   grep -vE 'JAVA_TOOL|ALSA|OpenAL|audio device|DEBUG' "$GAMELOG" | tail -n "${1:-20}" ;;
stop)  P='^[^ ]*java .*StartDesktopClient'; pkill -f "$P"; for _ in $(seq 20); do pgrep -f "$P" >/dev/null || exit 0; sleep 0.5; done; pkill -9 -f "$P"; true ;;
*)     sed -n '2,15p' "$0"; exit 1 ;;
esac
