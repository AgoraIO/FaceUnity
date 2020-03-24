//
//  FULiveCell.h
//  AgoraWithFaceunity
//
//  Created by ZhangJi on 2018/6/20.
//  Copyright © 2018 ZhangJi. All rights reserved.
//

#import <UIKit/UIKit.h>

@class FULiveModel;
@interface FULiveCell : UITableViewCell

@property (nonatomic, strong) FULiveModel *model;

@property (weak, nonatomic) IBOutlet UIImageView *modeImageView;
@property (weak, nonatomic) IBOutlet UIImageView *bottomImageView;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;

@end
