#!/usr/bin/env python3
"""Make a test copy of Necesse's res.data with every sound replaced by 0.1 s of silence.

Sounds are ~86% of res.data (276 of 320 MB on 1.3.3) and the headless client has no audio device, but the game
crashes when a sound is missing outright (the music player gets a null GameSound), so they are swapped for a tiny
silent Ogg Vorbis instead of being removed. Result: ~46 MB.

    python3 slim_res.py <path/to/res.data> <out.data>

res.data format (little-endian): int32 count, then per entry: int32 name length, UTF-8 name, int32 data length, data.
Standard library only, so it runs on the user's machine too.
"""
import base64
import struct
import sys

# Generated with: ffmpeg -f lavfi -i anullsrc=r=22050:cl=mono -t 0.1 -c:a libvorbis -q:a 0 silent.ogg
SILENT_OGG = base64.b64decode("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ABhwAAAIMKEMFBqyIgCIAgAAnHPOOeecc84555xzzjnnnHPOOecYY4wxxhhjjDHGGGOMMcYYY4wxxhhjjDHGGGOMMcYEAOxEOADsRFgIhYasBADCAQAAhBSCklIppZQSOeeklFJKKaWUyEEIpZRSSimlRNJJKaWUUkoppXFQSimllFJKKaGUUkoppZRSSgmllFJKKaWUUkoppZRSSimllFJKKaWUUkoppZRSSimllFJKKaWUUkoppZRSSimllFJKKaWUUkoppZRSSimllFJKKaWUUkoppZRSSimllFJKKaWUUkoBACYPDgBQCTbOsJJ0VjgaXGjISgAgNwAAUIo5xiSUkEpIJYQQSuUYhM5JCSm1VkIKrYQKOmido5BSS62VlEpJmYQQQiihhFJaKSW1UjIIoYRQSgghpVJKCaFlUEIKJZSUUkkttFRKySCEUFoJqZXUWgollZRBKamEklIqrbWUSkqtg9JSKa211kpKIZWWUgelpJZSKaW1FkprrbVOUiktpNZSa62VVkopnaWUSkmttZZaaymlVkIprbTSWikltdZSay2V1FpLraXWUmutpdZKKSWlllprrbWWWioptZRCKaWVkkJqqaXWSiothNBSSaWVVlprKaWUSigllZRaKqm1llJopYXSSkklpZZKKiml1FIqoZQSUiqhldRSa6mllkoqLbXUUiuplJZKSqkUAAB04AAAEGBEpYXYacaVR+CIQoYJKAAAEAQAGIiQmUCgAAoMZADAAUKCFABQWGAoXeiCECJIF0EWD1w4ceOJG07o0AYAGIiQmQChGCIkZAPABEWFdACwuMAoXeiCECJIF0EWD1w4ceOJG07o0AIBAAAAAAACAB8AAAcGEBHRXIbGBkeHxwdIiAgAAAAAAAAAAAAAAIBPZ2dTAASdCAAAAAAAABzEu6sCAAAA83mOFQYBAQEBAQEAAAAAAAA=")


def main(src, dst):
    with open(src, "rb") as f:
        data = f.read()
    count, = struct.unpack_from("<i", data, 0)
    pos, out, silenced = 4, [struct.pack("<i", count)], 0
    for _ in range(count):
        name_len, = struct.unpack_from("<i", data, pos)
        name = data[pos + 4:pos + 4 + name_len].decode("utf-8")
        header = data[pos:pos + 4 + name_len]
        pos += 4 + name_len
        size, = struct.unpack_from("<i", data, pos)
        body = data[pos + 4:pos + 4 + size]
        pos += 4 + size
        if name.endswith(".ogg"):
            body, silenced = SILENT_OGG, silenced + 1
        out.append(header + struct.pack("<i", len(body)) + body)
    if pos != len(data):
        sys.exit("unexpected res.data format: parsed %d of %d bytes" % (pos, len(data)))
    with open(dst, "wb") as f:
        f.write(b"".join(out))
    print("%d entries, %d sounds silenced -> %s" % (count, silenced, dst))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
