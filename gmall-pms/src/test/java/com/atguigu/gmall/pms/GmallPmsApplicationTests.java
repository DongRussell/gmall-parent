package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.mapper.ProductMapper;
import com.atguigu.gmall.pms.service.BrandService;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.vo.PageInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GmallPmsApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired
	ProductService productService;

	@Autowired
	ProductMapper productMapper;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	RedisTemplate<Object,Object> redisTemplateObj;
	@Test
	public void contextLoads() {
//		Product byId = productService.getById(1);
//		System.out.println(byId.getBrandName());
//		Brand brand = new Brand();
//		brand.setName("哈哈哈");
//		brandService.save(brand);
//		Product product = productMapper.selectById(26);
//		System.out.println(product.getBrandName());
		//Brand byId = brandService.getById(53);
		//System.out.println("保存成功。。。"+byId.getName());
		IPage<Product> page = productMapper.selectPage(new Page<Product>(5, 1), null);
		System.out.println(page.getTotal());
		//PageInfo pageInfo = new PageInfo(page.getTotal(), page.getPages(),
		//		5,page.getRecords(),1);


	}

	@Test
	public void RedisTemplate(){
		redisTemplate.opsForValue().set("hello","world");
		String hello = redisTemplate.opsForValue().get("hello");
		System.out.println(hello);
	}

	@Test
	public void RedisTemplateObj(){
		Brand brand = new Brand();
		brand.setName("hahaha");
		redisTemplateObj.opsForValue().set("abc",brand);
		System.out.println(brand);
		Brand abc = (Brand) redisTemplateObj.opsForValue().get("abc");
		System.out.println(abc.getName());
	}
}
