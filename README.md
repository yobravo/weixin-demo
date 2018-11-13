
0. Run "mvn install" to pack it into a jar file, and copy the packed jar file to a linux box.

1. to run it in linux box, copy these bash scripts in src/main/resources: start.sh stop.sh query.sh to that server box too, change file mode to executable. eg. chmod 755 start.sh stop.sh query.sh 
 
 Because weChat only access port 80(http) or 443(https), both need to root access to start, hence need to use sudo command:
 
 *  to start process
  sudo bash start.sh yourPackedJarFile.jar "-Dport=port443or80Only -DappId=yourWxAppId -DappSecret=yourWxAppSever -DserverToken=yourServerToken"
    
 *  to stop process. run ./stop.sh 
 
 * to query process. run ./query.sh
 
 
 2. after deployment, in weixin management backend website. 
 *  config the URL to be : https://linux-box-ip:443/wx or http://linux-box-ip:80/wx, up to what port you are using
 *  set the TOKEN same as whatever you have defined in the system property "-DserverToken={TOKEN}" when starting up the jar.
 reference to : https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421135319
 
可以关注订阅号： fancyquote， 然后发信息或图片留言，来查看该响应程序的回复
