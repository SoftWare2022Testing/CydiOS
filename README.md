# CydiOS
CydiOS: a model-based testing framework for iOS apps

## Apps used in experiment

The 50 apps used in the RQ3 can be found at **50appList.txt**


## iPhone extension

TO run the iPhone extension, we first need a jailbroken iPhone. The software version for iPhone should between 9.0 to 14.4.
After jailbreaking, install the Cydia(an unofficial appstore for jailbroken iOS devices), and install following tweaks in the cydia:
1. iOS Toolchain
2. OpenSSH
3. SUbstrate Safe mode
4. RocketBootstap
5. Theos Dependencies

![cydia](./cydia.jpeg=50x100)

run
```
make install
```
to install the iPhone extension(in the package folder） on a jailbroken iPhone
