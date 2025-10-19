import java.security.Permission;
import java.util.stream.Collectors;
import com.example.demo.model.Permission;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.model.User;

public class UserMapper {
    public static UserDTO userToUserDTO(User user) {
        return new UserDTO(user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().getAuthority(),
                user.getRole().getPermissions().stream().map(Permission::getAuthoriry).collect(Collectors.toSet()));
    }

    public static UserLoggedDto userLoggedDto(User user) {
        return new UserLoggedDto(user.getUsername(),
                user.getRole().getAuthoriry(),
                user.getRole().getPermissions().stream().map(Permission::getAuthoriry).collect(Collectors.toSet()));
    }
}
