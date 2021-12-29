//
//  FUBottomBar.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/11.
//

#import "FUBottomBar.h"

static NSString * const kFUBottomCellIdentifierKey = @"FUBottomCellIdentifier";

@interface FUBottomBar ()<UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>

@property (nonatomic, copy) NSArray<FUViewModel *> *viewModels;

/// 保存宽度
@property (nonatomic, copy) NSArray *itemWidths;

@property (nonatomic, copy) void (^operationHandler)(NSInteger item);

/// 当前选中的item
@property (nonatomic, assign) NSInteger selectedItem;

@end

@implementation FUBottomBar

- (instancetype)initWithFrame:(CGRect)frame viewModels:(NSArray *)viewModels moduleOperationHandler:(void (^)(NSInteger))handler {
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    flowLayout.minimumInteritemSpacing = 0;
    flowLayout.minimumLineSpacing = 0;
    flowLayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    self = [super initWithFrame:frame collectionViewLayout:flowLayout];
    if (self) {
        self.backgroundColor = [UIColor colorWithWhite:0 alpha:0.6];
        self.viewModels = [viewModels copy];
        self.operationHandler = handler;
        _selectedItem = -1;
        
        // 计算宽度
        NSMutableArray *tempWidths = [NSMutableArray arrayWithCapacity:self.viewModels.count];
        if (self.viewModels.count < 7) {
            // 平均分配宽度
            CGFloat width = CGRectGetWidth(frame) / self.viewModels.count * 1.0;
            for (NSInteger i = 0; i < self.viewModels.count; i++) {
                [tempWidths addObject:@(width)];
            }
        } else {
            // 根据文字适配宽度
            for (FUViewModel *viewModel in self.viewModels) {
                CGSize nameSize = [viewModel.model.name sizeWithAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:13]}];
                [tempWidths addObject:@(nameSize.width + 30)];
            }
        }
        self.itemWidths = [tempWidths copy];
        
        self.delegate = self;
        self.dataSource = self;
        self.showsHorizontalScrollIndicator = NO;
        self.showsVerticalScrollIndicator = NO;
        [self registerClass:[FUBottomCell class] forCellWithReuseIdentifier:kFUBottomCellIdentifierKey];
    }
    return self;
}

#pragma mark - Collection view data source
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.viewModels.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    FUBottomCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:kFUBottomCellIdentifierKey forIndexPath:indexPath];
    FUViewModel *viewModel = self.viewModels[indexPath.item];
    cell.name = viewModel.model.name;
    return cell;
}

#pragma mark - Collection view delegate
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    if (indexPath.item < 5) {
        [[FUAIKit shareKit] setMaxTrackFaces:4];
        
    }else{
            
        // 设置美体的时候
        [[FUAIKit shareKit] setMaxTrackFaces:1];
    }
    if (indexPath.item == _selectedItem) {
        // 隐藏模块
        [collectionView deselectItemAtIndexPath:indexPath animated:YES];
        _selectedItem = -1;
    } else {
        // 显示模块
        [collectionView selectItemAtIndexPath:indexPath animated:YES scrollPosition:UICollectionViewScrollPositionCenteredHorizontally];
        _selectedItem = indexPath.item;
    }
    !self.operationHandler ?: self.operationHandler(_selectedItem);
}

#pragma mark - Collection view delegate flow layout
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake([self.itemWidths[indexPath.item] floatValue], CGRectGetHeight(self.frame));
}

@end

@interface FUBottomCell ()

@property (nonatomic, strong) UILabel *nameLabel;

@end

@implementation FUBottomCell

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        [self.contentView addSubview:self.nameLabel];
    }
    return self;
}

#pragma mark - Setters
- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    self.nameLabel.textColor = selected ? [UIColor colorWithRed:94/255.f green:199/255.f blue:254/255.f alpha:1] : [UIColor whiteColor];
}

- (void)setName:(NSString *)name {
    self.nameLabel.text = name;
}

#pragma mark - Getters
- (UILabel *)nameLabel {
    if (!_nameLabel) {
        _nameLabel = [[UILabel alloc] initWithFrame:self.contentView.bounds];
        _nameLabel.textColor = [UIColor whiteColor];
        _nameLabel.font = [UIFont systemFontOfSize:13];
        _nameLabel.textAlignment = NSTextAlignmentCenter;
    }
    return _nameLabel;
}

@end
