#import "utils.h"
#import "AppTrace.h"


/*
该模块负责开启整个流程
*/

%hook UIWindow

static dispatch_once_t onceToken;


- (void)makeKeyAndVisible {
    %orig;

    dispatch_once(&onceToken, ^{
        [AppTrace letscreateHashSet];


        int numClasses;
        Class * classes = NULL;
        classes = NULL;
        numClasses = objc_getClassList(NULL, 0);
        if (numClasses > 0 ){
            classes = (__unsafe_unretained Class *)malloc(sizeof(Class) * numClasses);
            numClasses = objc_getClassList(classes, numClasses);
            for (int i = 0; i < numClasses; i++) {
                Class c = classes[i];
                NSBundle *b = [NSBundle bundleForClass:c];
                if (b != [NSBundle mainBundle]) continue;
                
                //NSLog(@"%s", class_getName(c));
                [AppTrace letsaddHashSet: [NSString stringWithFormat:@"%s", class_getName(c)]];
            }
            free(classes);
        }

        NSMutableArray* methodNumber = [NSMutableArray array];
        [methodNumber addObject:[NSNumber numberWithInt: numClasses]];
        [methodNumber writeToFile:METHOD_NUMBER_RECORD atomically:YES];


        [AppTrace setTraceFileCachePattern:TraceFileCachePatternSingle];
        [AppTrace startTrace];



        //NSFileManager * manager = [NSFileManager defaultManager];
        // 页面遍历历史记录
        //if ([manager fileExistsAtPath:PAGE_HISTORY]) {
        //    [manager removeItemAtPath:PAGE_HISTORY error:nil];
        //}
        // 行为列表
        //if ([manager fileExistsAtPath:ACTION_LIST]) {
        //    [manager removeItemAtPath:ACTION_LIST error:nil];
        //}
        // 文本集合
        //if ([manager fileExistsAtPath:TEXT_INFO]) {
        //    [manager removeItemAtPath:TEXT_INFO error:nil];
        //}
        // 界面UI视图树字典
        //if ([manager fileExistsAtPath:LAYOUT_INFO]) {
        //    [manager removeItemAtPath:LAYOUT_INFO error:nil];
        //}
        // 触发过的控件
        //if ([manager fileExistsAtPath:FINISHED_TASK]) {
        //    [manager removeItemAtPath:FINISHED_TASK error:nil];
        //}
        // 界面UI路径集合
        //if ([manager fileExistsAtPath:UIPATH_SET]) {
        //    [manager removeItemAtPath:UIPATH_SET error:nil];
        //}
        // 界面记录集合
        //if ([manager fileExistsAtPath:PAGE_RECORD]) {
        //    [manager removeItemAtPath:PAGE_RECORD error:nil];
        //}
        // 路径控制配置文件
        //if ([manager fileExistsAtPath:PATH_CONTROL]) {
        //    [manager removeItemAtPath:PATH_CONTROL error:nil];
        //}


        NSLog(@"----------------------------");
        //开启一个定时器，每3秒查询当前页面的
        NSTimer *timer = [NSTimer timerWithTimeInterval:2.5 target:[UIViewController class] selector:@selector(checkPageState) userInfo:nil repeats:YES];
        [[NSRunLoop currentRunLoop] addTimer:timer forMode:NSDefaultRunLoopMode];
    });
}

%end

;

