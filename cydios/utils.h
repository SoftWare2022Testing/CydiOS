// #import <WebKit/WebKit.h>
// #import "SimulateTouch.h"
// // #import <IOMobileFrameBuffer.h>
// // #import <QuartzCore/QuartzCore.h>
// // #import <IOSurface/IOSurface.h>
// // #import <IOSurface/IOSurfaceAccelerator.h>
// // #import "rocketbootstrap.h"
// #import "interface.h"
// #import "PTFakeTouch.h"
#import <WebKit/WebKit.h>
#import "SimulateTouch.h"
// #import <IOMobileFrameBuffer.h>
// #import <QuartzCore/QuartzCore.h>
// #import <IOSurface/IOSurface.h>
// #import <IOSurface/IOSurfaceAccelerator.h>
#import "rocketbootstrap.h"
#import "interface.h"
// #import "PTFakeMetaTouch.h"
#import "PTFakeTouch.h"

#define MAX_DEPTH 5
#define MAX_TABBAR_ACTION 30
#define SCREEN_WIDTH [UIScreen mainScreen].bounds.size.width
#define SCREEN_HEIGHT [UIScreen mainScreen].bounds.size.height
#define DocumentsPath [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject]
#define LAYOUT_INFO [DocumentsPath stringByAppendingPathComponent:@"layoutInfo.plist"]
#define TEXT_INFO [DocumentsPath stringByAppendingPathComponent:@"TextArray.plist"]
#define FINISHED_TASK [DocumentsPath stringByAppendingPathComponent:@"FinishedArray.plist"]
#define ACTION_LIST [DocumentsPath stringByAppendingPathComponent:@"EventArray.plist"]
#define PAGE_HISTORY [DocumentsPath stringByAppendingPathComponent:@"PageHistory.plist"]
#define UIPATH_SET [DocumentsPath stringByAppendingPathComponent:@"UIPath.plist"]
#define PAGE_RECORD [DocumentsPath stringByAppendingPathComponent:@"PageRecord.plist"]
#define METHOD_NUMBER_RECORD [DocumentsPath stringByAppendingPathComponent:@"MethodNumber.plist"]
#define PATH_CONTROL [DocumentsPath stringByAppendingPathComponent:@"PathControl.plist"]
#define STATIC_SCRIPT [DocumentsPath stringByAppendingPathComponent:@"cg.plist"]
#define PAGE_REDIRECT [DocumentsPath stringByAppendingPathComponent:@"PageRedirect.plist"]
#define FINISHED_TabBar [DocumentsPath stringByAppendingPathComponent:@"TabBarHistoryArray.plist"]
#define LAYOUT_INFO_EXPLORATION [DocumentsPath stringByAppendingPathComponent:@"layoutInfoUIexploration.plist"]
#define SUBVIEW_HIERARCHY [DocumentsPath stringByAppendingPathComponent:@"subviewHierarchy.plist"]
#define SUBVIEW_HIERARCHY_WITHOUT [DocumentsPath stringByAppendingPathComponent:@"subviewHierarchyWithoutIndex.plist"]
#define OVERALL_LAYOUT_INFO [DocumentsPath stringByAppendingPathComponent:@"overallLayoutInfo.plist"]
#define TIME_RECORD [DocumentsPath stringByAppendingPathComponent:@"TimeRecord.plist"]


@interface UIViewController()

// Executor.xm
+ (void)performClickAdvice:(NSMutableDictionary *)actionDict;
+ (void)performClickInPage:(NSString *)pageName;
+ (void)checkPageState;
+ (void)checkperformAPI:(NSString *)stateName;
+ (void)performAPIAdvice;
+(NSMutableSet *)getAllChildViewController:(UIViewController *)viewController;

// FetchLayout.xm
+ (UIViewController *)getVisibleViewController;
@end

@interface UIView()

// CollectingLayoutTree.xm
-(NSMutableDictionary *)processViewContent;
-(NSMutableArray *)getSubViews;

// FetchLayout.xm
+ (NSString *)fetchLayoutTreeAdvice;
+ (NSString *)fetchLayoutTree;
- (NSMutableDictionary *)fetchLayoutTreeInView;

// QuickSort.xm
- (void)swapInArray:(NSMutableArray *)arr withObject:(NSInteger)index1 andObject:(NSInteger)index2;
- (NSMutableArray *)partitionWith:(NSString *)key Of:(NSMutableArray *)arr withLeft:(NSInteger)L andRight:(NSInteger)R;
- (void)processQuickSortWith:(NSString *)key WithArr:(NSMutableArray *)arr withLeft:(NSInteger)L andRight:(NSInteger)R;

// GenerateEvent.xm
-(void)generateEventIn:(NSString *)Key;
- (NSMutableArray *)generateEventInDict:(NSMutableDictionary *)viewDict;

// CollectFeature.xm
-(BOOL)shouldRefreshFor:(NSString *)VCKey With:(NSMutableDictionary *)viewDict;
-(NSMutableArray *)collectTextInDict:(NSMutableDictionary *)viewDict;
-(void)collectPathFor:(NSString *)VCKey With:(NSMutableDictionary *)viewDict;
-(NSMutableArray *)collectPathInDict:(NSMutableDictionary *)viewDict;

// FindUITarget.xm
- (UIWindow *)getWindow;
-(BOOL)touchCompleted;
- (UIViewController *)getViewController;
-(NSString *)getResponderName;
-(NSMutableDictionary *)getViewActionInfo;

@end


@interface UIWindow()

// FindUITarget.xm
+(UIWindow *)getTopMostWindow;
-(UIView *)getTopMostView;
- (UIViewController *)processGetTopMostViewControllerIn:(UIViewController *)viewController;
-(UIViewController *)getTopMostViewController;
-(NSMutableArray *)getViewControllerHierarchy:(UIViewController *)viewController;
-(UIView *)processGetTopMostViewWith:(UIView *)contentView;
-(NSMutableArray *)getSubViewhierarchyAdvice:(UIView *)contentView;
+(void) processGetSubViewhierarchyAdvice:(UIView *)contentView;
- (NSMutableArray *)getSubViewsAdvice:(UIView *)contentView;
+ (NSMutableArray *)readScript;

@end