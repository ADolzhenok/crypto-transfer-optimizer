package com.vda.cryptotransferoptimizer.exceptions;

public class CoinNotFoundException extends RuntimeException{

    public CoinNotFoundException(String msg){
        super(msg);
    }

}
