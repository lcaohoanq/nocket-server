package com.lcaohoanq.nocket.domain.otp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService{

    private final OtpRepository otpRepository;

    @Override
    public Otp createOtp(Otp otp) {
        Otp newOtp = new Otp();
        newOtp.setOtp(otp.getOtp());
        newOtp.setEmail(otp.getEmail());
        newOtp.setExpiredAt(otp.getExpiredAt());
        newOtp.setUsed(otp.isUsed());
        newOtp.setExpired(otp.isExpired());
        return otpRepository.save(newOtp);
    }

    @Override
    public void disableOtp(UUID id) {
        Otp existingOtp = otpRepository.findById(id).orElse(null);
        if(existingOtp == null) return;
        existingOtp.setExpired(true);
        otpRepository.save(existingOtp);
    }

    @Override
    public Optional<Otp> getOtpByEmailOtp(String email, String otp) {
        return otpRepository.findByEmailAndOtp(email, otp);
    }

    @Override
    @Transactional
    public void setOtpExpired() {
        LocalDateTime now = LocalDateTime.now();
        // Update OTPs where expired_at < now and is_expired = 0
        otpRepository.updateExpiredOtps(now);
    }

}
