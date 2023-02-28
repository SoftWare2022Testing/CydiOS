
#import <Foundation/Foundation.h>
#import "SocketClient.h"
#import <sys/socket.h>
#import <netinet/in.h>
#import <arpa/inet.h>
#include <string.h>


NSString *receiveData= @"NONE";// 全局变量

@implementation SocketClient

+ (NSInteger)startSocket:(NSString*)msgTobeSend {
    //  dispatch_async(dispatch_get_global_queue(0, 0), ^{

        NSLog(@"Start Server-----------------------");
         
        int socketID = socket(AF_INET, SOCK_STREAM, 0);

        struct sockaddr_in socketAddr;
        socketAddr.sin_family = AF_INET;
        socketAddr.sin_port   = htons(8222);
        struct in_addr socketIn_addr;
        socketIn_addr.s_addr  = inet_addr("192.168.31.28");
        socketAddr.sin_addr   = socketIn_addr;

        connect(socketID, (const struct sockaddr *)&socketAddr, sizeof(socketAddr));
                  
        const char *msgStr = [msgTobeSend UTF8String];

         
        send(socketID, msgStr, strlen(msgStr), 0);

        uint8_t buffer[1024];
        ssize_t recvLen = recv(socketID, buffer, sizeof(buffer), 0);
  
        NSData *data = [NSData dataWithBytes:buffer length:recvLen];
        NSString *data_str= [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        receiveData = [[NSMutableString alloc] initWithString:data_str];
        NSLog(@"receiving %@",receiveData);

        if ([data_str rangeOfString:@"FUZZ"].location == NSNotFound) {
            return [receiveData intValue];
        } else {
            // NSLog(@"string contains bla!");
            return -1;
        }
}

@end
