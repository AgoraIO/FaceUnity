//
//  MainViewController.m
//  AgoraWithFaceunity
//
//  Created by ZhangJi on 11/03/2018.
//  Copyright © 2018 ZhangJi. All rights reserved.
//

#import "MainViewController.h"
#import "RoomViewController.h"

@interface MainViewController ()

@property (weak, nonatomic) IBOutlet UITextField *channelNameTextField;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    NSString *segueid = segue.identifier;
    
    if ([segueid isEqualToString: @"showRoom"]) {
        if (self.channelNameTextField.text == nil) {
            return;
        }
        RoomViewController *roomVC = segue.destinationViewController;
        roomVC.channelName = self.channelNameTextField.text;
    }
}

@end
