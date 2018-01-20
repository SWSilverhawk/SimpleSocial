package simone.rcl.shared;

public class SimpleSocialMessages {

    // Operations
    public static final int GET_SERVER_INFORMATION = 0;
    public static final int REGISTER_USER = 1;
    public static final int LOGIN_USER = 2;
    public static final int SEND_FRIEND_REQUEST = 3;
    public static final int REQUEST_FRIEND_LIST = 4;
    public static final int SEE_PENDING_FRIEND_REQUEST = 5;
    public static final int SEARCH_USER = 6;
    public static final int LOGOUT_USER = 7;

    // Register and Login
    public static final int OPERATION_COMPLETE = 0;
    public static final int USERNAME_ALREADY_IN_USE_ERROR = 1;
    public static final String USER_ALREADY_ONLINE_ERROR = "online";
    public static final String INVALID_USERNAME_OR_PASSWORD_ERROR = "invalid";

    // Send Friend Request
    public static final int REQUEST_ACCEPTED = 0;
    public static final int REQUEST_REFUSED = 1;
    public static final int REQUEST_DELAYED = 2;
    public static final int USER_NOT_ONLINE_ERROR = 3;
    public static final int SELF_REQUEST_ERROR = 4;
    public static final int USER_ALREADY_FRIEND_ERROR = 5;

    // See Pending Friend Request
    public static final int NO_FRIEND_REQUESTS = 1;

    // Follow User
    public static final int FOLLOW_FRIEND_SUCCESS = 0;
    public static final int USER_NOT_FRIEND_ERROR = 1;
    public static final int ALREADY_FOLLOW_ERROR = 2;
    public static final int USER_NOT_EXISTS_ERROR = 3;
    public static final int SELF_FOLLOW_ERROR = 4;

    // UserID
    public static final int EXPIRED_USER_ID_INT_ERROR = 6;
    public static final String EXPIRED_USER_ID_STRING_ERROR = "expired";

}
