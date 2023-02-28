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

once the iPhone extension is installed, you can found it at the cydia, and then you can open the target app for UI testing

![example](./cydia1.jpeg)


## Static analysis
The static analysis moudle can be found at the  **static** folder.
To run it, you need use CrackerXI+ or other tools to decrypt the IPA and then exetract the executable file (i.e., app binary).
After that, use ``scp ipAddressofYouriPhone@location_of_the_binary ./`` to transfer the executable file to your PC.

Then, run the following commands in a termnial window and you can get the analysis result:
```
cd static analysis
./cydios_simulate -m scan -i objc-msg-xref -f location_or_the_test_app_binary -d 'antiWrapper=1'
```


## Layout analysis
The code for layout analysis (to extract view controller transitions in the layout files) can be found under the **LayoutRead** folder,

just run the main.java file. 
