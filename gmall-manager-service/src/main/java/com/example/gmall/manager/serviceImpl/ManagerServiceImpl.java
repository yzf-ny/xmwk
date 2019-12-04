package com.example.gmall.manager.serviceImpl;/*
 *Author:yzf
 *Date:2019/11/30,13:02
 *ProjectName:gmall-parent
 **/

import Service.ManagerService;
import bean.*;
import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.manager.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManagerServiceImpl implements ManagerService {

    @Autowired
    BaseSaleAttrMapper saleAttrMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleValueMapper spuSaleValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    BaseCatalog1Mapper mapper1;

    @Autowired
    BaseCatalog2Mapper mapper2;

    @Autowired
    BaseCatalog3Mapper mapper3;

    @Autowired
    BaseAttrInfoMapper info;

    @Autowired
    BaseAttrValueMapper value;

    //一级分类
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return mapper1.selectAll();
    }

    //二级分类
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);

        return mapper2.select(catalog2);//如果有空字段，默认不作为条件
    }

    //三级分类
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 catalog3 = new BaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);
        return mapper3.select(catalog3);//如果有空字段，默认不作为条件
    }


    @Override
    public List<BaseAttrInfo> getAttrInfo(String catalog3Id) {

        Example example = new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id", catalog3Id);
        return info.selectByExample(example);

    }

    @Override
    @Transactional
    public Integer saveAttrInfo(BaseAttrInfo infos) {

        try {
            //处理属性平台
            if (infos.getId() != null && infos.getId().length() > 0) {
                info.updateByPrimaryKeySelective(infos);
            } else {
                infos.setId(null);
                info.insertSelective(infos);
            }
            //处理属性值
            //BaseAttrInfo select = info.selectOne(infos);
            System.out.println("id=" + infos.getId());
            //先删除相关信息才添加
            BaseAttrValue va = new BaseAttrValue();
            va.setAttrId(infos.getId());
            value.delete(va);
            List<BaseAttrValue> list = infos.getAttrValueList();
            if (list.size() > 0 && list != null) {
                for (BaseAttrValue v : list) {
                    v.setAttrId(infos.getId());
                    value.insertSelective(v);

                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrInfo base = new BaseAttrInfo();
        base.setId(attrId);
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo = info.selectByPrimaryKey(base);
        //查属性值集合
        BaseAttrValue attrValue = new BaseAttrValue();
        attrValue.setAttrId(attrId);
        baseAttrInfo.setAttrValueList(value.select(attrValue));
        return baseAttrInfo.getAttrValueList();
    }


    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return saleAttrMapper.selectAll();
    }

    @Override
    public int saveSpuInfo(SpuInfo spu) {

        try {
            //保存平台属性信息
            spuInfoMapper.insertSelective(spu);
            System.out.println("spuId="+spu.getId());
            //保存图片信息
            List<SpuImage> spuImageList = spu.getSpuImageList();
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spu.getId());
                spuImageMapper.insertSelective(spuImage);
            }
            //保存销售属性信息
            List<SpuSaleAttr> spuSaleAttrList = spu.getSpuSaleAttrList();
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spu.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //保存销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    spuSaleAttrValue.setSpuId(spu.getId());
                    spuSaleValueMapper.insertSelective(spuSaleAttrValue);
                }
            }

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<SpuInfo> spuList(Integer catalog3Id) {
        SpuInfo spu=new SpuInfo();
        spu.setCatalog3Id(catalog3Id+"");
        return spuInfoMapper.select(spu);
    }


}
