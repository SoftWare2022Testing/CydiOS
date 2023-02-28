#import "utils.h"
#import "OverallView.h"
#import "SocketClient.h"
#import "AppTrace.h"


/*
本模块的功能：
1. 每过几秒就检查页面UI元素，根据界面UI的情况做响应
2. 实现模拟点击操作
*/

%hook UIViewController

//要注意的是：在TabBarController上进入navigation是不会再看到TabBarController的，即为tabbarController = nil


%new 
+ (void)checkPageState {
    //dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    
    // 采集布局树
    NSLog(@"------------------------- start ---------------------------");

    NSString *stateName = [UIView fetchLayoutTreeAdvice];

    NSArray *stateNameArr = [stateName componentsSeparatedByString:@"-"];
    if (stateNameArr == nil || [stateNameArr count] != 2) {
        return;
    }
    NSString *responderName = stateNameArr[0];
    NSString *viewName = stateNameArr[1];
    NSLog(@"polyu checkPageState responderName: %@   viewName:%@ ",responderName,viewName);

    //页面跳转关系图
    NSMutableArray* pageRedirectArray = [NSMutableArray arrayWithContentsOfFile:PAGE_RECORD];
    if(pageRedirectArray == nil){
        pageRedirectArray = [NSMutableArray array];
    }
    if([pageRedirectArray indexOfObject:responderName] != NSNotFound){
    }else{
        NSTimeInterval interval = [[NSDate date] timeIntervalSince1970];
        [pageRedirectArray addObject: [NSString stringWithFormat: @"%ld", (long)interval]];
        [pageRedirectArray addObject: responderName];
    }
    [pageRedirectArray writeToFile:PAGE_RECORD atomically:YES];

    NSMutableDictionary *layoutInfo = [NSMutableDictionary dictionaryWithContentsOfFile: LAYOUT_INFO];
    if (layoutInfo == nil) {
        NSLog(@"in checkPageState layoutInfo is nil");
        layoutInfo = [NSMutableDictionary dictionary];
    }

    NSMutableArray *fetchedLayoutTree = layoutInfo[responderName];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_LOW, 0), ^{

    // ##### send to network
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:fetchedLayoutTree options:NSJSONWritingPrettyPrinted error:&error];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    NSInteger coverage = [AppTrace getCoverage];

    NSString *complete = [NSString stringWithFormat:@"%@-->%@-->%ld-->end", responderName, jsonString, coverage];
    int serverSelectedIndex = (int) [SocketClient startSocket: complete];

    NSMutableDictionary *finishedDict = [NSMutableDictionary dictionaryWithContentsOfFile:FINISHED_TASK];
    if (finishedDict == nil) {
        finishedDict = [NSMutableDictionary dictionary];
    }
 
    //int numberOfElements = [fetchedLayoutTree count];

    dispatch_async(dispatch_get_main_queue(), ^{
    
    if (serverSelectedIndex == -1){
        [self performAPIAdvice];
    }else{
        NSMutableDictionary *positionDict = fetchedLayoutTree[serverSelectedIndex];
        [self performClickAdvice:positionDict];

        NSMutableArray *finished_arrays = finishedDict[responderName];
        if (finished_arrays == nil) {
            finished_arrays = [NSMutableArray array];
        }

        if([finished_arrays indexOfObject:positionDict[@"Index"]] != NSNotFound){

        }else{
            NSTimeInterval interval = [[NSDate date] timeIntervalSince1970];
            [finished_arrays addObject: [NSString stringWithFormat: @"%ld", (long)interval]];
            [finished_arrays addObject: positionDict[@"Index"]];
        }
        
        [finishedDict setObject:finished_arrays forKey:responderName];

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_LOW, 0), ^{
            [finishedDict writeToFile: FINISHED_TASK atomically:YES];
        });
    }

    });
    });
}


%new 
+ (void)performClickAdvice:(NSMutableDictionary *)actionDict
{   
    NSString *strPoint = actionDict[@"center"]; 
    CGPoint point = CGPointFromString(strPoint);
    NSString *gestureStr = actionDict[@"action"];
    NSLog(@"actionDict is %@", actionDict);

    if ([gestureStr isEqualToString:@"pan"]) {
        // 手势是滑动
        CGPoint pointSwipeTo = CGPointMake(point.x, point.y - 60);
        //if (![actionDict[@"direction"] isEqualToString:@"vertical"]) {
        //    pointSwipeTo = CGPointMake(point.x - 30, point.y);
        //}
        NSLog(@"begin to pan %@ from %@ to %@", actionDict[@"direction"], strPoint, NSStringFromCGPoint(pointSwipeTo));

        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseMoved];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseEnded];
    } else {
        NSLog(@"----------Tap-Tap-Tap-Tap---------");
        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];;
        pointId = [PTFakeTouch fakeTouchId:pointId AtPoint:point withTouchPhase:UITouchPhaseEnded];
        NSLog(@"End PTFakeTouch touch at %@ pointId:%d", strPoint, (int)pointId);
        
    }

}


%new
+(NSMutableSet *)getAllChildViewController:(UIViewController *)viewController 
{
    UIViewController *topMostViewController = viewController;

    NSMutableSet *controllerSet = [[NSMutableSet alloc] init];
    [controllerSet addObject:NSStringFromClass([viewController class])];

    if ([viewController respondsToSelector:@selector(topViewController)]) {
        // rootViewController是NavigationController
        UINavigationController *navigationController = (UINavigationController *)viewController;
        UIViewController* childViewController = [navigationController topViewController];
        [controllerSet unionSet:[UIViewController getAllChildViewController: childViewController]];
    }

    else if ([viewController respondsToSelector:@selector(selectedViewController)]) {
        // rootViewController是TabBarController
        UITabBarController *tabBarController = (UITabBarController *)viewController;
        UIViewController *childViewController = tabBarController.selectedViewController;
        [controllerSet unionSet:[UIViewController getAllChildViewController: childViewController]];
    }
    else {
        for (UIViewController *childController in viewController.childViewControllers ){
            [controllerSet unionSet:[UIViewController getAllChildViewController: childController]];
        }
    }

    UIViewController *presentedVC = topMostViewController.presentedViewController;
    if (presentedVC != nil) {
        [controllerSet unionSet:[UIViewController getAllChildViewController: presentedVC]];
    }
    return controllerSet;
}



%new 
+ (void)performAPIAdvice
{   
   double screen_Width = [UIScreen mainScreen].bounds.size.width;
   double screen_Height = [UIScreen mainScreen].bounds.size.height;

    int r = arc4random_uniform(100);

    if(r > 90){
        // Swipe
        CGPoint startPoint = CGPointMake(screen_Width/2, screen_Height/2);
        CGPoint pointSwipeTo = CGPointMake(startPoint.x, startPoint.y - 60);
        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:startPoint withTouchPhase:UITouchPhaseBegan];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseMoved];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseEnded];
    }else{
        int randomX = arc4random_uniform(screen_Width);
        int randomY = arc4random_uniform(screen_Height);
        CGPoint tapPoint = CGPointMake(randomX,randomY);

        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:tapPoint withTouchPhase:UITouchPhaseBegan];;
        pointId = [PTFakeTouch fakeTouchId:pointId AtPoint:tapPoint withTouchPhase:UITouchPhaseEnded];
        NSLog(@"click at %@", NSStringFromCGPoint(tapPoint));
    }
}



// 在VC中模拟点击
%new 
+ (void)performClickInPage: (NSString *)stateName {
    
    NSArray *stateNameArr = [stateName componentsSeparatedByString:@"-"];
    if (stateNameArr == nil || [stateNameArr count] != 2) {
        return;
    }
    NSString *responderName = stateNameArr[0];
    NSString *viewName = stateNameArr[1];
    NSLog(@"+++ stateName : %@ responderName: %@ viewName: %@", stateName, responderName, viewName);
    
    /*
        actionInfo存储了该应用所有需要触发的事件
        Key是ViewController或者UIWindow
        Value是viewActionInfo，用来表征页面的事件
    */
    NSMutableDictionary *actionInfo = [NSMutableDictionary dictionaryWithContentsOfFile:ACTION_LIST];
    if (actionInfo == nil) {
        actionInfo = [NSMutableDictionary dictionary];
    }

    NSLog(@"polyu ");
    NSLog(@"polyu print actionInfo in performClickInPage:");
    NSLog(@"%@",actionInfo);
    NSLog(@"polyu print actionInfo in performClickInPage End");

    /*
        viewActionInfo存储一个页面需要触发的事件
        Key是ViewController的UIView类名
        Value是UI树
    */
    NSMutableDictionary *viewActionInfo = actionInfo[responderName];
    if(viewActionInfo == nil) {
        viewActionInfo = [NSMutableDictionary dictionary];
    }

    NSLog(@"polyu ");
    NSLog(@"polyu print viewActionInfo in performClickInPage:");
    NSLog(@"%@",viewActionInfo);
    NSLog(@"polyu print viewActionInfo in performClickInPage End");


    NSMutableArray *centerArray = [viewActionInfo[viewName] mutableCopy];
    if (centerArray == nil) {
        centerArray = [NSMutableArray array];
    }

    NSLog(@"polyu ");
    NSLog(@"polyu print centerArray in performClickInPage:");
    NSLog(@"%@",centerArray);
    NSLog(@"polyu print centerArray in performClickInPage End");
    

    /* 为我所用 */
    NSMutableDictionary *finishedDict = [NSMutableDictionary dictionaryWithContentsOfFile:FINISHED_TASK];
    if (finishedDict == nil) {
        finishedDict = [NSMutableDictionary dictionary];
    }
    NSMutableArray *finishedArray = [finishedDict[responderName] mutableCopy];
    if (finishedArray == nil) {
        finishedArray = [NSMutableArray array];
    }
    NSLog(@"in performClickInPage page %@ array count is %lu ", stateName, [centerArray count]);

    NSMutableDictionary *positionDict = centerArray[0];
    NSString *name = positionDict[@"name"];
    NSString *strPoint = positionDict[@"center"]; 
    CGPoint point = CGPointFromString(strPoint);
    NSString *gestureStr = positionDict[@"action"];
    NSString *text = positionDict[@"text"];
    NSLog(@"PTFakeTouch in %@ name is %@ point is %@ gesture is %@ text is %@", stateName, name,strPoint, gestureStr,text);
    NSLog(@"positionDict is %@", positionDict);

    if ([centerArray count] <= 1) {
        centerArray = [NSMutableArray array];
    } else {
        [centerArray removeObjectAtIndex:0];
    }
    [finishedArray addObject: positionDict];

    NSLog(@"==========================");
    viewActionInfo[viewName] = centerArray;
    actionInfo[responderName] = viewActionInfo;
    NSLog(@"viewName is %@ centerArray count is %d", viewName, (int)[centerArray count]);
    [actionInfo writeToFile:ACTION_LIST atomically:YES];
    // 存储已经触发过的控件，用于去重
    finishedDict[responderName] = finishedArray;
    [finishedDict writeToFile: FINISHED_TASK atomically:YES];

    NSLog(@"polyu ");
    NSLog(@"polyu print finishedDict write in performClickInPage:");
    NSLog(@"%@",finishedDict);
    NSLog(@"polyu print finishedDict write in performClickInPage End");


    if ([gestureStr isEqualToString:@"pan"]) {
        // 手势是滑动
        CGPoint pointSwipeTo = CGPointMake(point.x, point.y - 60);
        if (![positionDict[@"direction"] isEqualToString:@"vertical"]) {
            pointSwipeTo = CGPointMake(point.x - 30, point.y);
        }
        NSLog(@"begin to pan %@ from %@ to %@", positionDict[@"direction"], strPoint, NSStringFromCGPoint(pointSwipeTo));
        //NSInteger pointId = [PTFakeMetaTouch fakeTouchId:[PTFakeMetaTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];
        //[PTFakeMetaTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseMoved];
        //[PTFakeMetaTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseEnded];
        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseMoved];
        [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseEnded];

        // [UIView animateWithDuration:2.0 animations:^{
        //     [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseMoved];
        //     [PTFakeTouch fakeTouchId:pointId AtPoint:pointSwipeTo withTouchPhase:UITouchPhaseEnded];
        // }];
    } else {
        // 手势是点击
        //NSInteger pointId = [PTFakeMetaTouch fakeTouchId:[PTFakeMetaTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];;
        NSInteger pointId = [PTFakeTouch fakeTouchId:[PTFakeTouch getAvailablePointId] AtPoint:point withTouchPhase:UITouchPhaseBegan];;


        pointId = [PTFakeTouch fakeTouchId:pointId AtPoint:point withTouchPhase:UITouchPhaseEnded];
        //pointId = [PTFakeMetaTouch fakeTouchId:pointId AtPoint:point withTouchPhase:UITouchPhaseEnded];
    }
}


%end
