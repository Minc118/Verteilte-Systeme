package smtp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class RecipientValidator {
    private final Set<String> validRecipients = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "abc@def.edu",
            "ghi@jkl.com",
            "nmo@pqr.gov",
            "stu@vwx.de"
    )));

    boolean isValid(String recipient) {
        return validRecipients.contains(recipient);
    }
}
