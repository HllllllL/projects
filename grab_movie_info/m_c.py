import requests
from bs4 import BeautifulSoup

def getCo(url):
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info = soup.find_all("span", {"itemprop": "creator"})[0].text
	info = info.replace("\n","")
	return info

def imdb250List():
	result = []
	imdb250_Url = "http://www.imdb.com/chart/top?ref_=nv_ch_250_4"
	r = requests.get(imdb250_Url)
	soup = BeautifulSoup(r.content)
	movieData = soup.find_all("td", {"class": "titleColumn"})
	#counter=0
	for item in movieData:
		info = item.find_all("a")[0]
		urls = info.get('href')
		#name = info.text
		result.append("http://www.imdb.com"+urls)   #+"\t\t"+name
		#counter+=1
		#if(counter==10): break
	return result

def writeDat(fi,s):
	try:
		fi.write(s)
	except:
		fi.write(s.encode('utf-8'))

######

urlList = imdb250List()
m_counter = 0
co_dic = dict()
co_dat = open("/Users/haominglu/Desktop/4111/new_data/company.txt ","a+")
m_co = open("/Users/haominglu/Desktop/4111/new_data/produced_by.txt ","a+")

for url in urlList:
	company = getCo(url)
	if not(co_dic.has_key(company)):
		co_dic[company] = 1
		s1 = company+"\n"
		writeDat(co_dat,s1)
	m_coRelation=str(m_counter)+";"+company+"\n"
	writeDat(m_co,m_coRelation)
	m_counter+=1

