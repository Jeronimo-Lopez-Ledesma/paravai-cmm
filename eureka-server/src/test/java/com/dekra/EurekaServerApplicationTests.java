package com.dekra;

import com.dekra.eureka.EurekaServerApplication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EurekaServerApplicationTests {

	@Test
	void main() {
		assertDoesNotThrow(() -> EurekaServerApplication.main(new String[]{}));
	}

}
