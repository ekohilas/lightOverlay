import sys
from PIL import Image, ImageDraw, ImageFont
T=12  # px per tile
import os
ROOT=os.path.abspath(os.path.join(os.path.dirname(__file__),'..','..'))
OUT=os.path.join(ROOT,'docs','layouts')+'/'
torch=Image.open(os.path.join(ROOT,'src','main','resources','texture','torchoutline.png')).convert('RGBA')
icon=torch.resize((T*2,T*2),Image.NEAREST)
def tint(img,col):
    img=img.copy(); p=img.load()
    for y in range(img.height):
        for x in range(img.width):
            r,g,b,a=p[x,y]; p[x,y]=(r*col[0]//255,g*col[1]//255,b*col[2]//255,a)
    return img
icon=tint(icon,(255,190,60))
try: font=ImageFont.load_default(size=16); small=ImageFont.load_default(size=13)
except TypeError: font=small=ImageFont.load_default()
INFO={
 'diamondquadrant-17':('1. Diamond aligned to quadrant','distance 17 (optimal)'),
 'diamondaligned-17':('2. Diamond aligned','distance 17 (optimal)'),
 'diamond-18':('3. Diamond unaligned (default)','distance 18 (optimal), fewest lights'),
 'squarequadrant-8':('4. Square to quadrant','distance 8 (optimal)'),
 'square-8':('5. Square','distance 8 (optimal)'),
 'diamondquadrant-13':('Nice: diamond aligned to quadrant','distance 13'),
}
def draw(name):
    lines=open(os.path.join(sys.argv[1],name+'.txt')).read().split('\n')
    x0,y0,w,h,quad=map(int,lines[0].split())
    rows=[[tuple(map(int,c.split(':'))) for c in l.split()] for l in lines[1:1+h]]
    head=44
    img=Image.new('RGBA',(w*T,h*T+head),(24,26,30,255)); d=ImageDraw.Draw(img)
    lights=[]
    for y in range(h):
        for x in range(w):
            dist,border=rows[y][x]
            px,py=x*T,y*T+head
            if dist<0: lights.append((px,py)); dist=0
            if dist>9: col=(150,40,40)  # mobs can spawn
            else:
                f=(150-10*dist)/150.0  # torch light left at this tile
                col=(int(40+110*f),int(50+105*f),int(35+70*f))
            d.rectangle((px,py,px+T-1,py+T-1),fill=col)
    for y in range(h):
        for x in range(w):
            dist,border=rows[y][x]; px,py=x*T,y*T+head
            if border&1: d.line((px+T-1,py,px+T-1,py+T-1),fill=(235,240,230,200),width=2)
            if border&2: d.line((px,py+T-1,px+T-1,py+T-1),fill=(235,240,230,200),width=2)
    if quad:
        lx=(150-x0)*T; ly=(150-y0)*T+head
        for s in range(0,h*T,10): d.line((lx,head+s,lx,head+min(s+5,h*T)),fill=(255,230,60),width=2)
        for s in range(0,w*T,10): d.line((s,ly,min(s+5,w*T),ly),fill=(255,230,60),width=2)
    for (px,py) in lights: img.alpha_composite(icon,(px-T//2,py-T))
    d.rectangle((0,0,w*T,head-1),fill=(24,26,30,255))
    title,sub=INFO[name]
    d.text((8,4),title,fill=(255,255,255),font=font); d.text((8,24),sub,fill=(190,195,200),font=small)
    img.save(OUT+name+'.png'); return img
imgs=[draw(n) for n in INFO]
W,H=imgs[0].size; g=8
sheet=Image.new('RGBA',(2*W+3*g,3*H+4*g),(14,15,18,255))
for i,im in enumerate(imgs): sheet.alpha_composite(im,(g+(i%2)*(W+g),g+(i//2)*(H+g)))
sheet.save(OUT+'all.png'); print(sheet.size)
