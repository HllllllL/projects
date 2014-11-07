import requests
from bs4 import BeautifulSoup

def getActFromMovie(url,i):
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info = soup.find_all("div", {"itemprop": "actors"})
	acUrl = "http://www.imdb.com"+info[0].find_all("a")[i].get('href')
	return acUrl

def getRoleFromMovie(url,i):
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info = soup.find_all("td", {"class": "character"})[i].text
	name = info.split()
	s = ""
	for n in name:
		s=s+" "+n
	return s

def getActor(url):#first_name, last_name, gender, date_of_birth
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	info1 = soup.find_all("h1", {"class": "header"})[0].text
	name = info1.split()
	dob = ""
	bp = ""
	try:
		info2 = soup.find_all("time", {"itemprop": "birthDate"})[0]
		dob = info2.get('datetime')
		info3 = soup.find_all("div", {"id": "name-born-info"})[0]
		bp = info3.find_all("a")[3].text
	except:
		pass
	actorinfo=[]
	actorinfo.append(name[0])
	actorinfo.append(name[1])
	actorinfo.append(dob)
	actorinfo.append(bp)
	return actorinfo

def getMovie(url):
	r = requests.get(url)
	soup = BeautifulSoup(r.content)
	g_data = soup.find_all("h1", {"class": "header"})

	name = ""
	year = ""
	length = ""
	genre = ""
	for item in g_data:
		name = item.find_all("span",{"class":"itemprop"})[0].text
		year = item.find_all("span",{"class":"nobr"})[0].text
		year = year.replace("(","").replace(")","")
	length = soup.find_all("time",{"itemprop":"duration"})[0].text.replace(" ", "")
	length = length.replace("min", "").replace("\n","")
	genre = soup.find_all("span",{"itemprop":"genre"})[0].text
	movie = name+";"+length+";"+genre+";"+year

	m_a_list = []
	m_a_list.append(movie)
	info = soup.find_all("div", {"itemprop": "actors"})
	acUrl1 = "http://www.imdb.com"+info[0].find_all("a")[0].get('href')
	acUrl2 = "http://www.imdb.com"+info[0].find_all("a")[1].get('href')
	m_a_list.append(acUrl1)
	m_a_list.append(acUrl2)
	return m_a_list;

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

urlList = imdb250List()
m_counter = 0
a_counter = 0
r_counter = 0
a_dic = dict()
m_dat = open("/Users/haominglu/Desktop/4111/new_data/movie.txt ","a+")
a_dat = open("/Users/haominglu/Desktop/4111/new_data/actor.txt ","a+")
r_dat = open("/Users/haominglu/Desktop/4111/new_data/role.txt ","a+")
m_a = open("/Users/haominglu/Desktop/4111/new_data/acts_in.txt ","a+")
writeDat(r_dat,"movie_id;actor_id;role_id\n")

for url in urlList:
	infolist = getMovie(url)
	s = str(m_counter) + ";" + infolist[0]+"\n"
	writeDat(m_dat,s)

	acUrl1 = infolist[1]
	ac1 = getActor(acUrl1)
	role1 = str(r_counter)+";"+getRoleFromMovie(url,0)+"\n"
	writeDat(r_dat,role1)
	ac1name = ac1[0]+ac1[1]
	if not(a_dic.has_key(ac1name)):
		a_dic[ac1name] = a_counter
		s1 = str(a_counter) + ";" + ac1[0]+";"+ac1[1]+";"+ac1[2]+";"+ac1[3]+"\n"
		writeDat(a_dat,s1)
		m_aRelation1=str(m_counter)+";"+str(a_counter)+";"+str(r_counter)+"\n"
		writeDat(m_a,m_aRelation1)
		a_counter+=1
	else:
		actor1_id = a_dic.get(ac1name)
		m_aRelation1=str(m_counter)+";"+str(actor1_id)+";"+str(r_counter)+"\n"
		writeDat(m_a,m_aRelation1)
	r_counter+=1

	acUrl2 = infolist[2]
	ac2 = getActor(acUrl2)
	role2 = str(r_counter)+";"+getRoleFromMovie(url,1)+"\n"
	writeDat(r_dat,role2)
	ac2name = ac2[0]+ac2[1]
	if not(a_dic.has_key(ac2name)):
		a_dic[ac2name] = a_counter
		s2 = str(a_counter) + ";" + ac2[0]+";"+ac2[1]+";"+ac2[2]+";"+ac2[3]+"\n"
		writeDat(a_dat,s2)
		m_aRelation2=str(m_counter)+";"+str(a_counter)+";"+str(r_counter)+"\n"
		writeDat(m_a,m_aRelation2)
		a_counter+=1
	else:
		actor2_id = a_dic.get(ac2name)
		m_aRelation2=str(m_counter)+";"+str(actor2_id)+";"+str(r_counter)+"\n"
		writeDat(m_a,m_aRelation2)
	r_counter+=1

	m_counter+=1

m_dat.close()
a_dat.close()
r_dat.close()
m_a.close()

