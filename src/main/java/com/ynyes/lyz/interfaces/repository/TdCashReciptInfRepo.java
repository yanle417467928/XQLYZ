package com.ynyes.lyz.interfaces.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.ynyes.lyz.interfaces.entity.TdCashReciptInf;

public interface TdCashReciptInfRepo extends PagingAndSortingRepository<TdCashReciptInf, Long>, JpaSpecificationExecutor<TdCashReciptInf> 
{
	List<TdCashReciptInf> findByOrderHeaderId(Long headerId);
	List<TdCashReciptInf> findByOrderNumber(String orderNumber);
}

