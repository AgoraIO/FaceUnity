//
//  MainViewController.m
//  BeautifyExample
//
//  Created by support on 2020/9/16.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "MainViewController.h"

#import "ViewController.h"


@interface MainViewController ()

@property (weak, nonatomic) IBOutlet UITextField *channelNameTF;


@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];

//    self.view.backgroundColor = [UIColor whiteColor];
    
}

/// 输入房间号开始跳转

- (IBAction)joinBtnClick:(UIButton *)sender {
    
    if (self.channelNameTF.text.length <= 0) {
        
        NSLog(@"请先输入房间号");
        return;
    }
    
    ViewController *roomVC = [[ViewController alloc] init];
    roomVC.channelName = self.channelNameTF.text;
    roomVC.modalPresentationStyle = 0;
    [self presentViewController:roomVC animated:YES completion:nil];
    
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    NSString *segueid = segue.identifier;
    
    if ([segueid isEqualToString: @"showRoom"]) {
        if (self.channelNameTF.text == nil) {
            return;
        }
        ViewController *roomVC = segue.destinationViewController;
        roomVC.channelName = self.channelNameTF.text;
    }
}


- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{

    [self.channelNameTF resignFirstResponder];
    
}



@end
