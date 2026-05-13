
package com.example.dacs3.admin.scheduler;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OfferExpiryScheduler {

    @Autowired
    private Firestore firestore;

    // Chạy mỗi 1 phút
    @Scheduled(fixedDelay = 60_000)
    public void expireOffers() {
        try {
            List<QueryDocumentSnapshot> docs = firestore.collection("tours")
                    .whereEqualTo("isOffer", true).get().get().getDocuments();

            long now = System.currentTimeMillis();

            for (QueryDocumentSnapshot doc : docs) {
                String timeLeft = doc.getString("timeLeft");
                if (timeLeft == null || timeLeft.isEmpty()) continue;
                try {
                    long expiry = Long.parseLong(timeLeft);
                    if (expiry > 0 && now > expiry) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("isOffer", false);

                        Object origNL = doc.get("originalPrice");
                        Object origTE = doc.get("originalPriceChild");
                        Object origTN = doc.get("originalPriceInfant");

                        if (origNL != null && getLong(origNL) > 0) updates.put("price",     getLong(origNL));
                        if (origTE != null && getLong(origTE) > 0) updates.put("giaTreEm",  getLong(origTE));
                        if (origTN != null && getLong(origTN) > 0) updates.put("giaTreNho", getLong(origTN));

                        firestore.collection("tours").document(doc.getId()).update(updates).get();
                        System.out.println("Tour " + doc.getId() + " đã hết ưu đãi, khôi phục giá gốc.");
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getLong(Object p) {
        if (p instanceof Long)    return (Long) p;
        if (p instanceof Integer) return ((Integer) p).longValue();
        return 0L;
    }
}