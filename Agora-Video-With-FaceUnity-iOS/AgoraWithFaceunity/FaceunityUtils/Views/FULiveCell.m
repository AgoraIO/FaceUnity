//
//  FULiveCell.m
//  AgoraWithFaceunity
//
//  Created by ZhangJi on 2018/6/20.
//  Copyright © 2018 ZhangJi. All rights reserved.
//

#import "FULiveCell.h"
#import "FULiveModel.h"

@implementation FULiveCell

-(instancetype)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        
    }
    return self ;
}


-(void)setModel:(FULiveModel *)model {
    _model = model ;
    
    self.titleLabel.text = NSLocalizedString( _model.title, nil) ;
    self.modeImageView.image = [UIImage imageNamed:_model.title];
    self.bottomImageView.image = _model.enble ? [UIImage imageNamed:@"bottomImage"] : [UIImage imageNamed:@"bottomImage_gray"] ;
}


@end
