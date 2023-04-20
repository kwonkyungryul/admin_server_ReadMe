package shop.readmecorp.adminserverreadme.modules.user.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private Boolean isMembership;

    private Boolean isAutoPayment;

    private List<MultipartFile> files;

    private String status;
}
