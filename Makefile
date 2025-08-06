THEOS_DEVICE_IP = 192.168.31.165
# THEOS_DEVICE_PORT=2222
export ARCHS = arm64 
TARGET = iphone:13.6:10.0 
SYSROOT = $(THEOS)/sdks/iPhoneOS13.6.sdk

include $(THEOS)/makefiles/common.mk

TWEAK_NAME = travseiosapp
travseiosapp_FILES = Tweak.xm AppTrace.m SocketClient.m CheckScript.xm GenerateEvent.xm Executor.xm DealwithException.xm CollectingLayoutTree.xm CollectFeature.xm QuickSort.xm FetchLayout.xm FindUITarget.xm
travseiosapp_FRAMEWORKS = UIKit WebKit PTFakeTouch
travseiosapp_EXTRA_FRAMEWORKS = PTFakeTouch
# travseiosapp_LDFLAGS = -lsimulatetouch 
travseiosapp_LIBRARIES = rocketbootstrap AppTraceTom
travseiosapp_PRIVATE_FRAMEWORKS = AppSupport ChatKit IMFoundation IDS IMCore IOKit 
${TWEAK_NAME}_CFLAGS = -I./header/
include $(THEOS_MAKE_PATH)/tweak.mk
# travseiosapp_CFLAGS = -std=c++11 -stdlib=libc++
# travseiosapp_LDFLAGS = -stdlib=libc++
travseiosapp_LDFLAGS += -F./static/
travseiosapp_CFLAGS  += -F./

after-install::
	install.exec "killall -9 SpringBoard"
