import requests
from bs4 import BeautifulSoup

def getDirFromMovie(url):
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info = soup.find_all("div", {"itemprop": "director"})
	dirUrl = "http://www.imdb.com"+info[0].find_all("a")[0].get('href')
	return dirUrl

def getDir(url):#first_name, last_name, gender, date_of_birth
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info1 = soup.find_all("h1", {"class": "header"})[0].text
	name = info1.split()
	dob = ""
	try:
		info2 = soup.find_all("time", {"itemprop": "birthDate"})[0]
		dob = info2.get('datetime')
	except:
		pass
	dirInfo = name[0]+";"+name[1]+";male;"+dob
	d_list = []
	d_list.append(dirInfo)
	dirName = name[0]+name[1]
	d_list.append(dirName)
	return d_list

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
d_counter = 0
m_dic = dict()
d_dic = dict()
d_dat = open("/Users/haominglu/Desktop/4111/new_data/director.txt ","a+")
m_d = open("/Users/haominglu/Desktop/4111/new_data/directed_by.txt ","a+")

for url in urlList:
	dUrl = getDirFromMovie(url)
	d_list = getDir(dUrl)
	if not(d_dic.has_key(d_list[1])):
		d_dic[d_list[1]] = d_counter
		s1 = str(d_counter) + ";" + d_list[0]+"\n"
		writeDat(d_dat,s1)
		m_dRelation=str(m_counter)+";"+str(d_counter)+"\n"
		writeDat(m_d,m_dRelation)
		d_counter+=1
	else:
		director_id = d_dic.get(d_list[1])
		m_dRelation=str(m_counter)+";"+str(director_id)+"\n"
		writeDat(m_d,m_dRelation)
	m_counter+=1

