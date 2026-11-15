(function () {
    var token = window.__CSRF_TOKEN__;
    if (!token) return;

    function inject(form) {
        if (!form || (form.method || '').toLowerCase() !== 'post') return;
        if (form.querySelector('input[name="_csrf"]')) return;
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = '_csrf';
        input.value = token;
        form.appendChild(input);
    }

    document.addEventListener('submit', function (e) {
        inject(e.target);
    }, true);

    var nativeSubmit = HTMLFormElement.prototype.submit;
    HTMLFormElement.prototype.submit = function () {
        inject(this);
        return nativeSubmit.apply(this, arguments);
    };

    var nativeFetch = window.fetch;
    if (nativeFetch) {
        window.fetch = function (input, init) {
            init = init || {};
            var headers = new Headers(init.headers || {});
            headers.set('X-CSRF-TOKEN', token);
            init.headers = headers;
            return nativeFetch(input, init);
        };
    }
})();
