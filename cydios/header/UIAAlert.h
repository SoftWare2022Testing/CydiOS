//
//     Generated by class-dump 3.5 (64 bit).
//
//     class-dump is Copyright (C) 1997-1998, 2000-2001, 2004-2013 by Steve Nygard.
//

#import "UIAElement.h"
#import "UIAButton.h"

@interface UIAAlert : UIAElement
{
}

+ (id)toOneRelationshipKeys;
+ (id)_moreToOneRelationshipKeys;
+ (Class)_classForSimpleUIAXElement:(id)arg1;
- (UIAButton*)defaultButton;
- (UIAButton*)cancelButton;
- (UIAElementArray*)buttons;
- (NSString*)name;
- (id)_nameFromChildForAXElement:(id)arg1;

@end

