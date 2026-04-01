package com.example.dacs3.admin.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dsh6wv90k", 
                "api_key", "857374939747862",
                "api_secret", "zwAdSA9JbPpX8r_AeHA_F8g91xU"
        ));
    }
}
