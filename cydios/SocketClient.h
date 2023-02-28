#import <Foundation/Foundation.h>

extern NSString *receiveData;


@interface SocketClient : NSObject



// 开始检测
+ (NSInteger)startSocket:(NSString*)msgTobeSend;

@end

