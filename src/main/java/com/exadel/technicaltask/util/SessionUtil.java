package com.exadel.technicaltask.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class SessionUtil {
    @Autowired
    private HttpSession session;

    List<String> justFlashedAttributeNames = new ArrayList<>();
    List<String> usedFlashAttributeNames = new ArrayList<>();

    @PreDestroy
    private void cleanup() {
        for (String usedFlashAttributeName : this.usedFlashAttributeNames) {
            if (!this.justFlashedAttributeNames.contains(usedFlashAttributeName)) {
                session.removeAttribute(usedFlashAttributeName);
            }
        }
    }

    public void flash(String attributeName, Object value) {
        this.justFlashedAttributeNames.add(attributeName);

        session.setAttribute(attributeName, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String attributeName, T defaultValue) {
        if (!this.usedFlashAttributeNames.contains(attributeName)) {
            this.usedFlashAttributeNames.add(attributeName);
        }

        Object attributeValue = session.getAttribute(attributeName);

        if (attributeValue == null) {
            return defaultValue;
        }

        return (T) attributeValue;
    }

    public <T> T get(String attributeName) {
        return this.get(attributeName, null);
    }
}
