//
//  hookObjcSend.h
//  fishhookdemo
//
//  Created by FanFamily on 2018/7/21.
//  Copyright © 2018年 Family Fan. All rights reserved.
//

#ifndef hookObjcSend_h
#define hookObjcSend_h


//static 修饰全局变量的时候，这个全局变量只能在本文件中访问，不能在其它文件中访问，即便是 extern 外部声明也不可以。


static int method_min_duration = 1 * 1000; // 1 milliseconds

static int connect_id;

void createHashSet(void);

void addClassName(char * classname);

//void lcs_start(lcs_startz);
void lcs_start(char* log_path);
void lcs_stop_print(void);
void lcs_resume_print(void);

char* read_buffer(void);

int getCurrentCoverage(void);

void freeMemory(char* tofree_ptr);

#endif /* hookObjcSend_h */
