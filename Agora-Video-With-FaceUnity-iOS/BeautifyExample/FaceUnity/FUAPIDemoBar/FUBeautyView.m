//
//  FUBeautyView.m
//  FUAPIDemoBar
//
//  Created by L on 2018/6/27.
//  Copyright © 2018年 L. All rights reserved.
//

#import "FUBeautyView.h"
#import "UIImage+demobar.h"
#import "UIColor+FUAPIDemoBar.h"

@interface FUBeautyView ()<UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>

/// 恢复按钮
@property (nonatomic, strong) UIButton *recoverButton;

@end

@implementation FUBeautyView
@synthesize selectedIndex = _selectedIndex;
@synthesize dataList = _dataList;

-(void)awakeFromNib {
    [super awakeFromNib];
    
    self.backgroundColor = [UIColor clearColor];
    self.delegate = self ;
    self.dataSource = self ;
    [self registerClass:[FUBeautyCell class] forCellWithReuseIdentifier:@"FUBeautyCell"];
    
    _selectedIndex = -1;
}


#pragma mark ---- UICollectionViewDataSource

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataList.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    FUBeautyCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"FUBeautyCell" forIndexPath:indexPath];
    
    if (indexPath.row < self.dataList.count){
        FUBaseModel *model = self.dataList[indexPath.row] ;
        NSString *imageName ;
        
            BOOL opened = YES;
        
        if (model.iSStyle101) {
            opened = fabs([model.mValue floatValue] - 0.5) > 0.01 ? YES : NO;
        }else{
            opened = fabsf([model.mValue floatValue] - 0) > 0.01 ? YES : NO;
        }
        
        
            BOOL selected = _selectedIndex == indexPath.row ;
            
            if (selected) {
                imageName = opened ? [model.imageName stringByAppendingString:@"-3.png"] : [model.imageName stringByAppendingString:@"-2.png"] ;
            }else {
                imageName = opened ? [model.imageName stringByAppendingString:@"-1.png"] : [model.imageName stringByAppendingString:@"-0.png"] ;
            }

        cell.imageView.image = [UIImage imageWithName:imageName];
        cell.titleLabel.text = NSLocalizedString(model.mTitle,nil);
        cell.titleLabel.textColor = _selectedIndex == indexPath.row ? [UIColor colorWithHexColorString:@"5EC7FE"] : [UIColor whiteColor];
    }
    return cell ;
}

#pragma mark ---- UICollectionViewDelegate
-(void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    if (_selectedIndex == indexPath.row) {
        return ;
    }
    FUBaseModel *model = self.dataList[indexPath.row];
    _selectedIndex = indexPath.row ;
    
    [self reloadData];
    
    if (self.mDelegate && [self.mDelegate respondsToSelector:@selector(beautyCollectionView:didSelectedParam:)]) {
        [self.mDelegate beautyCollectionView:self didSelectedParam:model];
    }
}

#pragma mark ---- UICollectionViewDelegateFlowLayout

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    
    return UIEdgeInsetsMake(16, 16, 6, 16) ;
}


- (UIButton *)recoverButton {
    if (!_recoverButton) {
        _recoverButton = [UIButton buttonWithType:UIButtonTypeCustom];
    }
    return _recoverButton;
}


@end


@implementation FUBeautyCell

-(instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, frame.size.width, frame.size.width)];
        self.imageView.layer.masksToBounds = YES ;
        self.imageView.layer.cornerRadius = frame.size.width / 2.0 ;
        [self addSubview:self.imageView];
        
        self.titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(-10, frame.size.width + 2, frame.size.width + 20, frame.size.height - frame.size.width - 2)];
        self.titleLabel.textAlignment = NSTextAlignmentCenter ;
        self.titleLabel.textColor = [UIColor whiteColor];
        self.titleLabel.font = [UIFont systemFontOfSize:10];
        self.titleLabel.adjustsFontSizeToFitWidth = YES;
        
        [self addSubview:self.titleLabel ];
    }
    return self ;
}

@end
