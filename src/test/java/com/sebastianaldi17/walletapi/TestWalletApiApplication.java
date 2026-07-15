package com.sebastianaldi17.walletapi;

import org.springframework.boot.SpringApplication;

public class TestWalletApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(WalletApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
