##http服务器： 
###实验实现要求  
 
1. 要求实现HTTP  协议服务器，提供个人主页服务，要求服务器能够为很多人提供网页服务。 
2. 每个人有一个学号， 按学号命名主页文件的名字。 
3. 实现的功能包括GET, DELETE, OPTIONS,HEAD。
4. GET 获取一个人的网页， DELETE删除一个人的网页， OPTIONS提供那些已经实现的操作,HEAD获取请求头信息。 

###功能介绍  
1. 整体功能  
&emsp;&emsp;通过浏览器或者自己实现的网页抓取程序访问该服务器，可以采用的访问方式分别有GET,DELETE,OPTIONS,HEAD。通过这些方式的访问会返回相应的信息。  
&emsp;&emsp;在服务器端，服务器会接收来自客户端的请求，并分析客户端的请求，并作出相应的相应。为了便于服务器端的管理，这里会记录服务器运行和客户端访问的日志，生成的日志会保存在服务器中的一个名为“服务器日志”的文件夹中。  
    
2. 基本功能  
<div align="center">
<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/%E5%9F%BA%E6%9C%AC%E5%8A%9F%E8%83%BD.png" alt="基本功能" width="50%" height="50%"/>
</div>
3. 程序内各个类的功能  
	&emsp;&emsp;**``JHTTPd``**是服务器程序，启动自后会将运行的主机作为一个服务器，并将指定的文件目录作为服务器根目录为访问的用户提供学生个人主页服务。 
	<div align="center">
	<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/%E5%90%84%E4%B8%AA%E7%B1%BB%E7%9A%84%E5%8A%9F%E8%83%BD.png" width="50%" height="50%"/>
	</div>
	&emsp;&emsp;**``WebClient``**是一个简单的类似于浏览器的网页抓取客户端，用于向服务器发出GET,DELETE,HEAD,OPTIONS,ERRORREQUEST请求，其中的ERRORREQUEST请求方式是用于测试如果采用服务器不提供的请求方式请求的时候会返回哪些内容。如果单纯地使用浏览器访问主机，只能得到GET方式获得的数据，而无法看到相应头信息的内容，所以我实现了一个简单的客户端，用于查看这些访问方式返回的头信息。 
	
###代码框架
1. ``HttpForPersonPages``代码结构(左)   ``RequestProcessor``代码结构(右)  
<div align="center">
	<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/HttpForPersonPages.png" width="18%" height="18%"/>
	<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/RequestProcessor.png" width="50%" height="50%"/>
</div>

2. 具体操作的详细过程
<div align="center">
	<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/DELETE.png" width="30%" height="30%"/>
	<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/GET.png" width="30%" height="30%"/>
  <img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/HEAD.png" width="30%" height="30%"/>
</div>

3. 程序亮点  
  1. 使用多线程和线程池。 
使得服务器可以同时为多个客户端提供服务，同时限制过多的连接而造成资源的过多消耗，有效防止服务器奔溃。 
  2. 访问路径检测。  
对文件的路径进行判断，防止客户端的恶意连接而访问到服务器目录以外的其他文件。 
  3. 使用底层的输出流。 
    由于网页含有的内容很多，即客户端请求的文件类型很多，除了文本类型的html文件外，还有图片、视屏等文件。所以不使用字符流，而使用底层的OutputStream可以有效的返回请求文件。 
  4. 更好的获取content-type方法。 
    如果仅仅使用下面的方法获取文件的content-type是不够完善的。 
    ``URLConnection.getFileNameMap().getContentTypeFor(fileName);``
    如：css文件的content-type无法获取。这样会造成无法解析文件，导致文件显示不正确，对于css文件，上面的方法会返回null,最终导致浏览器无法解析样式表，网页样式丢失。在本程序中，我结合上面的方法以及文件的拓展名和多种MIME类型来获取content-type，更加全面。 
  5. 服务器日志输出。 
    对于一个服务，为了更加好的维护，对一些访问和错误进行记录。这里使用的方法是服务器日志输出。日志会统一放到一个服务器日志的目录中，并且每一份日志都是以当日的时间按照格式：yyyy-MM-dd来命名的，便于查找。 
  6. 独立写了一个图形界面的程序WebClient。 
    由于浏览器无法不显示返回的头信息，所以通过浏览器无法查看DELETE,HEAD和OPTIONS请求和未实现请求方式返回的信息，同时也无法看到GET请求返回的头信息。所以通过WebClient，我们可以选择自己想要请求的方式，并在WebClient中看到服务器放回的头信息，正文信息。更能直观的了解服务器与客户端的交互过程。  
  7. ``index.html``文件。  
    index.html是服务器指定的默认主页，如果用户提供具体的路径，则会按照访问主页处理。在index.html中会展示服务器所含有的学生个人主页列表，并且也实现了GET，DELETE，HEAD，OPTIONS和ERRORTYPE（用于测试未实现方式），用户可以通过点击按钮测试。由于这部分的访问中主要通过jQuery的ajax实现，所以部分信息的处理会显示在浏览器的控制台，而不是浏览器主窗口，需要用F12按键打开调试界面进行查看。

###代码测试  
1. 浏览器测试  
&emsp;&emsp;可以在地址栏输入localhost直接进入默认主页。当然你也可以用localhost/index.html来进行访问。主页将显示服务器所含有的学生的列表，你可以点击学生名通过GET方式访问其主页，或者选择相应的请求操作学生主页。可以结合浏览器的开发者工具测试。  

2. WebClient测试    
&emsp;&emsp;可以运行中的WebClient程序，并在输入框中输入地址，选择想要的访问方式进行访问。这种方式容易查看请求信息。    
<div align="center">
<img src="https://github.com/DoneSpeak/interesting_java/blob/master/Http/imageForReadme/webClient%E8%AE%BF%E9%97%AEindex.html.png" width="50%" height="50%">
</div>


	



