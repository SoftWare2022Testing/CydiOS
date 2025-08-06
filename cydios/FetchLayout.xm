#import "utils.h"

%hook UIViewController

/**
+ (UIViewController *)getVisibleViewController
功能：
1. 获取当前应当处理的UI元素
2. 对UI元素的子控件列表进行优先由上到下，其次由左到右的排序
*/
%new
+ (UIViewController *)getVisibleViewController {
    // 1. 获取topmostwindow
    UIWindow *window = [UIWindow getTopMostWindow];
    //2. 获取topmostviewcontroller
    UIViewController *topmostController = [window getTopMostViewController];
    NSLog(@"getVisibleViewController: %@", NSStringFromClass([topmostController class]));

    return topmostController;
}

%end


/**
本模块的作用在于提取UI视图树
*/

%hook UIView
%new
+ (NSString *)fetchLayoutTreeAdvice {
    // 1. 获取当前覆盖优先级最高的topmostView
    UIWindow *targetWindow = [UIWindow getTopMostWindow];
    
    UIWindow * screenwindow = [[[UIApplication sharedApplication] delegate] window];

    double screenWdith = [UIScreen mainScreen].bounds.size.width;
    double screenHeight = [UIScreen mainScreen].bounds.size.height;
    

    NSLog(@"polyu targetWindow in fetchLayoutTree:%@",NSStringFromClass([targetWindow class]));

    UIView *targetView = [targetWindow getTopMostView];

    NSLog(@"polyu targetView in fetchLayoutTree:%@",NSStringFromClass([targetView class]));
    
    NSString *viewName = NSStringFromClass([targetView class]);

    UIWindow *keyWindow = [UIApplication sharedApplication].keyWindow;
    UIViewController *topMostViewController = [keyWindow getTopMostViewController];
    NSLog(@"polyu topMostViewController in fetchLayoutTreeAdvice:%@", NSStringFromClass([topMostViewController class]));


    NSString *responderName = NSStringFromClass([topMostViewController class]);
    NSLog(@"responderName in fetchLayoutTreeAdvice:%@",responderName);


    //----------------开始了-------------------------
    NSMutableArray* subviews = [targetWindow getSubViewsAdvice:targetView];

    NSMutableArray* getSubView_result = [targetWindow getSubViewhierarchyAdvice:targetView];
    NSMutableDictionary* subviewsHierarchyDict = getSubView_result[0];

    //--------------------------------------------------------

    // 需要点击的控件
    //TODO:@"UIImageView" "UILabel"的注册问题：1
    NSSet *tapSet = [[NSSet alloc] initWithObjects:@"UIButton", @"UITabBarButton", @"UITableViewCell", @"UICollectionViewCell", @"UIAlertControllerActionView",@"UISegment", @"UISegmentedControl" ,@"UIView", nil];
    // 需要滑动的控件
    NSSet *panSet = [[NSSet alloc] initWithObjects:@"UIScrollView", @"UITableView", @"UICollectionView",  @"UIWebView", nil];

    NSSet *imageSet = [[NSSet alloc] initWithObjects:@"UIImageView",@"UILabel", nil];

    NSSet *switchSet = [[NSSet alloc] initWithObjects:@"UISwitch", nil];

    NSMutableArray *result = [NSMutableArray array];

    
    // #################################  Navigation Bar上的控件们

    UINavigationController *navigationController = topMostViewController.navigationController;
    if(navigationController!=NULL){
        NSMutableArray* navigationbarSubviews = [targetWindow getSubViewsAdvice:[navigationController navigationBar]];

        for(UIView* barview in [[navigationbarSubviews reverseObjectEnumerator] allObjects]){
            if ([barview isKindOfClass: [UIButton class]]) {
                [subviews addObject:barview];
            }
        
            else if ([barview isKindOfClass: [UISearchBar class]]) {
                ((UISearchBar *) barview).text = @"0000000000";
            }
            else if ([barview isKindOfClass: [UITextField class]]) {
                //UITextField* tempptr = (UITextField* ) barview;
                //tempptr.text = @"0000000000";
                ((UITextField *) barview).text = @"0000000000";
            }
        }
    }

    UITabBarController *tabbarController = topMostViewController.tabBarController;
    if (tabbarController!=NULL){
        NSMutableArray* tabbarSubviews = [targetWindow getSubViewsAdvice:[tabbarController tabBar]];
        for(UIView* tabBarView in tabbarSubviews){
            if([NSStringFromClass([tabBarView class]) isEqualToString:@"UITabBarButton"]){
                [subviews addObject:tabBarView];
            }
        }
    }


    // #################################  Navigation Bar上的控件们


    //-------------------------------------------------------------------------------------------------
    //2. 将UI元素的信息存储到字典中
    for (UIView * subview in subviews)
    {   
        if (subview.isHidden == TRUE){
            continue;
        }

        if(subview.frame.size.width == 0 || subview.frame.size.height == 0){
            continue;
        }

        NSString *name = NSStringFromClass([subview class]);
        
        NSString *text = nil;

        if ([name rangeOfString:@"UIBarBackground"].location != NSNotFound) {
            continue;
        }

        if([NSStringFromClass([subview class]) isEqualToString:@"UITabBarButton"]){
            name = @"UITabBarButton";
        }
    
        else if ([subview isKindOfClass: [UIButton class]] || [name hasSuffix: @"Button"]) {
            name = @"UIButton";
            if ([subview respondsToSelector:@selector(titleLabel)]) {
                UILabel *label = ((UIButton *) subview).titleLabel;
                text = label.text;
            }
        } else if ([subview isKindOfClass: [UILabel class]]) {
            name = @"UILabel";
            if ([subview respondsToSelector:@selector(text)]) {
                text = ((UILabel *) subview).text;
            }
        } else if ([subview isKindOfClass: [UITableViewCell class]]) {
            name = @"UITableViewCell";
        } else if ([subview isKindOfClass: [UICollectionViewCell class]]) {
            name = @"UICollectionViewCell";
        } else if ([subview isKindOfClass: [UITableView class]]) {
            name = @"UITableView";
        } else if ([subview isKindOfClass: [UICollectionView class]]) {
            name = @"UICollectionView";
        } else if ([subview isKindOfClass: [UIWebView class]]) {
            name = @"UIWebView";
        } else if ([subview isKindOfClass: [UIScrollView class]]) {
            name = @"UIScrollView";
        } else if ([subview isKindOfClass: [UIImageView class]]) {
            name = @"UIImageView";
        }else if ([subview isKindOfClass: [UINavigationBar class]]) {
            name = @"UINavigationBar";
        } else if ([subview isKindOfClass: [UITabBar class]]) {
            name = @"UITabBar";
        } else if ([name containsString: @"UIAlertControllerActionView"]) {
            name = @"UIAlertControllerActionView";
        } else if ([subview isKindOfClass: [UISegmentedControl class]]) {
            name = @"UISegmentedControl";
        } else if ([name hasSuffix: @"Segment"]) {
            name = @"UISegment";
        }else if ([subview isKindOfClass: [UISearchBar class]]) {
            name = @"UISearchBar";
            if ([subview respondsToSelector:@selector(text)]) {
                ((UISearchBar *) subview).text = @"0000000000";
            }
        }
        else if ([subview isKindOfClass: [UITextField class]]) {
            name = @"UITextField";
            if ([subview respondsToSelector:@selector(text)]) {
                ((UITextField *) subview).text = @"0000000000";
            }
        }
        else if ([subview isKindOfClass: [WKWebView class]]) {
            name = @"WKWebView";
        }
        else if ([subview isKindOfClass: [UISwitch class]]) {
            name = @"UISwitch";
        }
        else if ([subview isKindOfClass: [UIView class]]) {
            name = @"UIView";
        }
        else{
            continue;
        }
    
        NSMutableDictionary *actionInfo = [NSMutableDictionary dictionary];
        // 2.1 控件类型
        actionInfo[@"name"] = name;
        actionInfo[@"target"] = NSStringFromClass([subview class]);
    
        // 2.3 如果包含文本
        if (text != nil) {
            actionInfo[@"text"] = text;
        }
    
        if (subview.accessibilityIdentifier != nil){
                actionInfo[@"accessibilityIdentifier"] = subview.accessibilityIdentifier;
        }
        if (subview.accessibilityLabel != nil){
            actionInfo[@"accessibilityLabel"] = subview.accessibilityLabel;
        }


        // 2.7 UI控件中心点，即接受不了UI交互的坐标
        //CGPoint rootViewPoint  = [subview.superview convertPoint: subview.center toView: nil];
        //CGPoint rootViewPoint =[subview convertPoint:subview.center toView:nil];
        //CGPoint rootViewPoint  = [subview center];
    
        // 2.8 记录UI元素的宽和高
        NSInteger width = subview.frame.size.width;
        NSInteger height = subview.frame.size.height;
        NSString *widgetSize = [NSString stringWithFormat: @"Width:%ld,Height:%ld", (long)width, (long)height];
        actionInfo[@"size"] = widgetSize;

        CGRect rectInScreen =[subview convertRect:subview.bounds toView:screenwindow];
        actionInfo[@"frame"] = NSStringFromCGRect(rectInScreen);

        if(rectInScreen.origin.x + rectInScreen.size.width > screenWdith){
            continue;
        }

        CGPoint rootViewPoint = CGPointMake(CGRectGetMidX(rectInScreen), CGRectGetMidY(rectInScreen));
        if((rectInScreen.origin.y + rectInScreen.size.height) > screenHeight){
            rootViewPoint.y = (rectInScreen.origin.y + screenHeight) / 2;
        }

        NSString *strRootViewPoint = NSStringFromCGPoint(rootViewPoint);
        actionInfo[@"center"] = strRootViewPoint;

        CGFloat xValue = rectInScreen.origin.x;
        CGFloat yValue = rectInScreen.origin.y;


        NSMutableArray* array_1 = subviewsHierarchyDict[[NSValue valueWithNonretainedObject:subview]];

        if(array_1!=nil){
            //将array数组转换为string字符串
            actionInfo[@"Index"] = [[array_1 valueForKey:@"description"] componentsJoinedByString:@"--"];
        }else if ([name isEqualToString: @"UITabBarButton"]){
            actionInfo[@"Index"] = [NSString stringWithFormat:@"TabBar-%@-%@",name, strRootViewPoint];
        }else{
            actionInfo[@"Index"] = [NSString stringWithFormat:@"NavBar-%@-%@",name, strRootViewPoint];
        }

        
        //NSLog(@"UIScreen Width:%f,UIScreen Height:%f",screenWdith, screenHeight);

        //
        if (xValue >= 0 && xValue <= screenWdith && yValue >= 0 && yValue <= screenHeight) {
            if ([tapSet containsObject: name]) {
                actionInfo[@"action"] = @"tap";
                [result addObject: actionInfo];
            } 
            else if([imageSet containsObject: name]){
                actionInfo[@"action"] = @"tap";
                [result addObject: actionInfo];
            }
            else if ([panSet containsObject: name]) {
                actionInfo[@"action"] = @"pan";
                if (width > height) {
                    actionInfo[@"direction"] = @"horizontal";
                } else {
                    actionInfo[@"direction"] = @"vertical";
                }
                [result addObject: actionInfo];
            }
            else if ([switchSet containsObject: name]){
                actionInfo[@"action"] = @"tap";
                [result addObject: actionInfo];
            }
            else{
                actionInfo[@"action"] = @"null";
                [result addObject: actionInfo];
            }
        }
    
    }

    NSMutableDictionary *layoutInfo = [NSMutableDictionary dictionaryWithContentsOfFile: LAYOUT_INFO];
    if (layoutInfo == nil) {
        layoutInfo = [NSMutableDictionary dictionary];
    }
    // viewLayout 保存具体某一个ViewController相关的视图，其中key为视图名，Value为字符串形式的布局
    layoutInfo[responderName] = result;
    [layoutInfo writeToFile: LAYOUT_INFO atomically:YES];

    NSString *layoutKey = [NSString stringWithFormat:@"%@-%@", responderName, viewName];
    return layoutKey;
}


+ (NSString *)fetchLayoutTree {
    // 1. 获取当前覆盖优先级最高的topmostView
    UIWindow *targetWindow = [UIWindow getTopMostWindow];
    
    NSLog(@"polyu targetWindow in fetchLayoutTree:%@",NSStringFromClass([targetWindow class]));

    UIView *targetView = [targetWindow getTopMostView];

    NSLog(@"polyu targetView in fetchLayoutTree:%@",NSStringFromClass([targetView class]));

    if (targetView == nil) {
        // 此时说明可以触发的UI控件都已经触发完毕，需要借助API返回
        //TODO: 为什么

        //TODO:
        //[UIViewController performAPI];
        return nil;
    }

    
    NSString *viewName = NSStringFromClass([targetView class]);

    // 2. 获取字典形式的当前视图，存储在layoutTreeDict中
    // 2.1 获取托管topmostView的ViewController
    id responder = [targetView getViewController];
    if (responder == nil) {
        // 如果当前Window没有添加ViewController托管，那么将当前UIWindow存为responder
        responder = [targetView getWindow];
        if (responder == nil) {
            return nil;
        }
    }
    NSString *responderName = NSStringFromClass([responder class]);

    NSLog(@"responderName in fetchLayoutTree:%@",NSStringFromClass([responder class]));


    /* 
        layoutInfo保存具体该应用所有的视图布局信息，其中key为ViewController或者UIWindow的类名,
        Value为一个viewLayout是一个字典，key是各个View的名字，Value是布局
    */
    NSMutableDictionary *layoutInfo = [NSMutableDictionary dictionaryWithContentsOfFile: LAYOUT_INFO];
    if (layoutInfo == nil) {
        layoutInfo = [NSMutableDictionary dictionary];
    }
    // viewLayout 保存具体某一个ViewController相关的视图，其中key为视图名，Value为字符串形式的布局
    NSMutableDictionary *viewLayout = layoutInfo[responderName];
    if (viewLayout == nil) {
        viewLayout = [NSMutableDictionary dictionary];
    }
    NSMutableDictionary *layoutTreeDict = [targetView fetchLayoutTreeInView];
    //[targetView collectPathFor:responderName With:layoutTreeDict];

    // 3.将字典形式的主界面UIView转换为json数据格式, 并存入字典
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:layoutTreeDict options:NSJSONWritingPrettyPrinted error:nil];
    NSString *jsonStr = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
    viewLayout[viewName] = jsonStr;
    layoutInfo[responderName] = viewLayout;
    [layoutInfo writeToFile: LAYOUT_INFO atomically:YES];


    // 4. 生成当前界面相关的UI交互列表
    // 如果尚未生成过此View的UI交互列表，必须进入generateEventIn的逻辑
    BOOL firstCheckView = NO;
    // 根据界面文案判断是否需要更新UI交互列表
    BOOL needRefresh = [targetView shouldRefreshFor:responderName With:layoutTreeDict];
    NSMutableDictionary *actionInfo = [NSMutableDictionary dictionaryWithContentsOfFile:ACTION_LIST];
    if (actionInfo == nil) {
        firstCheckView = YES;
    } else {
        NSMutableDictionary *viewActionInfo = actionInfo[responderName];
        if (viewActionInfo == nil) {
            firstCheckView = YES;
        } else {
            NSMutableArray *centerArray = viewActionInfo[viewName];
            if (centerArray == nil) {
                firstCheckView = YES;
            }
        }
    }
    if (needRefresh || firstCheckView) {
        [targetView generateEventIn: responderName];
    }
    NSString *layoutKey = [NSString stringWithFormat:@"%@-%@", responderName, viewName];
    return layoutKey;
}

%new
- (NSMutableDictionary *)fetchLayoutTreeInView {
    NSMutableDictionary *viewDict=[self processViewContent];

    NSLog(@"polyu ");
    NSLog(@"polyu print viewDict in fetchLayoutTreeInView:");
    NSLog(@"%@",viewDict);
    NSLog(@"polyu print viewDict in fetchLayoutTreeInView End");

    // 此处原计划接入其他功能，目前尚未添加
    return viewDict;
}


%end