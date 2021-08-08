//
//  FUBaseView.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FUBaseViewModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface FUBaseCollectionView : UICollectionView
@property (nonatomic, strong) FUBaseViewModel *viewModel;

//当前选中的索引
@property (nonatomic, assign) NSInteger selectedIndex;

//数据源
@property (nonatomic, strong) NSArray *dataList;
@end

NS_ASSUME_NONNULL_END
