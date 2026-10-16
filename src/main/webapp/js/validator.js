const Validator = {
    patterns: {
        email: /^[a-zA-Z0-9._%+-]+@gmail\.com$/,
        phone: /^0\d{9}$/,
        username: /^(?=.{3,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$/,
        password: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&]).{8,}$/,
        fullName: /^[a-zA-ZÀ-ỹ\s]{2,100}$/
    },

    messages: {
        email: 'Email không hợp lệ!',
        phone: 'Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số!',
        username: 'Tên đăng nhập 3-20 ký tự, không chứa ký tự đặc biệt ngoại trừ dấu chấm và gạch dưới.',
        password: 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ cái, số và ký tự đặc biệt!',
        confirmPassword: 'Mật khẩu xác nhận không khớp!',
        fullName: 'Họ tên phải từ 2 ký tự trở lên và không chứa số!',
        required: 'Vui lòng điền đầy đủ thông tin!'
    },

    isValid: function(type, value) {
        if (!this.patterns[type]) {
            console.error(`Không tìm thấy regex cho loại: ${type}`);
            return false;
        }
        return this.patterns[type].test(value);
    }
};