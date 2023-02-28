//
//  AppTrace.m
//  AppTrace
//
//  Created by chenzhengxu on 2019/4/10.
//

#import "AppTrace.h"
#include "AppTraceImpl.h"

static TraceFileCachePattern kFileCachePattern = 0;
static int kAppTraceStarted = 0;

@implementation AppTrace

+ (void)setTraceFileCachePattern:(TraceFileCachePattern)pattern {
    kFileCachePattern = pattern;
}

+ (void)letscreateHashSet{
    createHashSet();
}

+ (void)letsaddHashSet:(NSString*)className{
    const char *str2=[className UTF8String];
    //NSLog(@"---%@",className);

    addClassName((char*)str2);
    // if((char*)str2 != ""){
    //     addClassName((char*)str2);
    // }
}



+ (void)startTrace {
    if (kAppTraceStarted != 0) {
        lcs_resume_print();
        return;
    }
    kAppTraceStarted = 1;
    NSString *rootdir = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES)[0];
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *work_dir = [rootdir stringByAppendingPathComponent:@"apptraceTom"];
    if (![fm fileExistsAtPath:work_dir]) {
        [fm createDirectoryAtPath:work_dir withIntermediateDirectories:YES attributes:nil error:nil];
    }
    // NSTimeInterval timeInterval = [[NSDate date] timeIntervalSince1970];
    NSString *log_name = [NSString stringWithFormat:@"trace_testapp.txt"];
    char *log_path = (char *)[[work_dir stringByAppendingPathComponent:log_name] UTF8String];
    
    NSLog(@"Log OutPut File is %@", log_name);
    lcs_start(log_path);
    
}

+ (void)endTrace {
    lcs_stop_print();
}

+ (NSInteger)getCoverage{
    return getCurrentCoverage();
}


+ (CGFloat) getFileSize
{
    NSString *rootdir = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES)[0];
    NSString *work_dir = [rootdir stringByAppendingPathComponent:@"apptraceTom"];
    NSString *log_name = [NSString stringWithFormat:@"trace_testapp.txt"];
    NSString *log_path = [work_dir stringByAppendingPathComponent:log_name];

    NSFileManager *fileManager = [[NSFileManager alloc] init];
    float filesize = -1.0;
    if ([fileManager fileExistsAtPath:log_path]) {
        NSDictionary *fileDic = [fileManager attributesOfItemAtPath:log_path error:nil];
        unsigned long long size = [[fileDic objectForKey:NSFileSize] longLongValue];
        filesize = 1.0*size/1024;
    }
    return filesize;
}


+ (void)setMinDuration:(int)minDuration {
    method_min_duration = minDuration * 1000;
}

@end
