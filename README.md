# CydiOS
CydiOS: a model-based testing framework for iOS apps

## Apps used in experiment

The 50 apps used in the RQ3 can be found at **50appList.txt**


## iPhone extension

TO run the iPhone extension, we first need a jailbroken iPhone. The software version for iPhone should between 9.0 to 14.4.
After jailbreaking, install the Cydia(an unofficial appstore for jailbroken iOS devices), and install some tweaks in the cydia:
1. OpenSSH
2. SUbstrate Safe mode
3. Theos Dependencies

To install our iPhone extension, run the following commands in a termnial window (the computer should connect to the same local netwrok with the iphone ): 
```
cd cydios
make package 
make install
```
note that 
1. you should specify the bundle ID of the test app in **travseiosapp.plist**
2. you should change the ip address of the iPhone in **Makefile**, like THEOS_DEVICE_IP = 192.168.31.174

once the iPhone extension is installed, you can found it at the cydia:
![example](./cydia.jpeg)
