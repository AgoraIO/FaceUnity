//
//  FUBeautyNodeModelProvider.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/27.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyNodeModelProvider.h"

@interface FUBeautyNodeModelProvider ()
@property (nonatomic, strong) FUBeautySkinNodeModelProvider *skinProvider;
@property (nonatomic, strong) FUBeautyFilterNodeModelProvider *filterProvider;
@property (nonatomic, strong) FUBeautyShapeNodeModelProvider *shapeProvider;
@end

@implementation FUBeautyNodeModelProvider
@synthesize dataSource = _dataSource;

- (instancetype)init {
    self = [super init];
    if (self) {
        _skinProvider = [FUBeautySkinNodeModelProvider instanceProducer];
        _filterProvider = [FUBeautyFilterNodeModelProvider instanceProducer];
        _shapeProvider = [FUBeautyShapeNodeModelProvider instanceProducer];
    }
    return self;
}


- (id)dataSource {
    return _dataSource;
}

@end
