# Genera app/src/main/assets/locations.txt a partir de countries-states-cities-database (ODbL).
# Uso: descarga csv-cities.csv.gz de https://github.com/dr5hn/countries-states-cities-database/releases,
# descomprímelo aquí y ejecuta: python3 generate.py ../../app/src/main/assets/locations.txt
import csv,collections,unicodedata,os,sys
NAMES={"AR":"Argentina","BO":"Bolivia","CL":"Chile","CO":"Colombia","CR":"Costa Rica","CU":"Cuba","DO":"República Dominicana","EC":"Ecuador","SV":"El Salvador","GT":"Guatemala","HN":"Honduras","MX":"México","NI":"Nicaragua","PA":"Panamá","PY":"Paraguay","PE":"Perú","PR":"Puerto Rico","ES":"España","UY":"Uruguay","VE":"Venezuela","US":"Estados Unidos"}
MERGE={("PE","Municipalidad Metropolitana de Lima"):"Lima"}
CAP=150
skip={'abandoned','historical_capital'}
def key(s): return unicodedata.normalize('NFD',s).encode('ascii','ignore').decode().lower()
data=collections.defaultdict(lambda: collections.defaultdict(dict))
for r in csv.DictReader(open('csv-cities.csv',encoding='utf-8')):
    cc=r['country_code']
    if cc not in NAMES or r['type'] in skip: continue
    name=r['name'].strip(); st=r['state_name'].strip()
    if not name or not st or '|' in name: continue
    st=MERGE.get((cc,st),st)
    pop=int(float(r['population'] or 0))
    d=data[cc][st]; d[name]=max(pop,d.get(name,0))
out=[]; total=0
for cc in sorted(NAMES,key=lambda c:key(NAMES[c])):
    out.append(f"#{cc}|{NAMES[cc]}")
    for st in sorted(data[cc],key=key):
        top=sorted(data[cc][st].items(),key=lambda kv:-kv[1])[:CAP]
        out.append("@"+st); out+=sorted((n for n,_ in top),key=key); total+=len(top)
open(sys.argv[1],'w',encoding='utf-8').write("\n".join(out)+"\n")
print(total,os.path.getsize(sys.argv[1]))
