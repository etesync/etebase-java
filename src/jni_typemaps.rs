mod swig_foreign_types_map {}

foreign_typemap!(
    ($p:r_type) <T> Result<T> => swig_i_type!(T) {
        $out = match $p {
            Ok(x) => {
                swig_from_rust_to_i_type!(T, x, ret)
                ret
            }
            Err(err) => {
                let msg = err.to_string();
                let exception_class = match err {
                    Error::UrlParse(_) => swig_jni_find_class!(ETEBASE_UrlParseException, "com/etebase/client/exceptions/UrlParseException"),
                    Error::MsgPack(_) => swig_jni_find_class!(ETEBASE_MsgPackException, "com/etebase/client/exceptions/MsgPackException"),
                    Error::ProgrammingError(_) => swig_jni_find_class!(ETEBASE_ProgrammingErrorException, "com/etebase/client/exceptions/ProgrammingErrorException"),
                    Error::Padding(_) => swig_jni_find_class!(ETEBASE_PaddingException, "com/etebase/client/exceptions/PaddingException"),
                    Error::Base64(_) => swig_jni_find_class!(ETEBASE_Base64Exception, "com/etebase/client/exceptions/Base64Exception"),
                    Error::Encryption(_) => swig_jni_find_class!(ETEBASE_EncryptionException, "com/etebase/client/exceptions/EncryptionException"),
                    Error::Unauthorized(_) => swig_jni_find_class!(ETEBASE_UnauthorizedException, "com/etebase/client/exceptions/UnauthorizedException"),
                    Error::Conflict(_) => swig_jni_find_class!(ETEBASE_ConflictException, "com/etebase/client/exceptions/ConflictException"),
                    Error::PermissionDenied(_) => swig_jni_find_class!(ETEBASE_PermissionDeniedException, "com/etebase/client/exceptions/PermissionDeniedException"),
                    Error::NotFound(_) => swig_jni_find_class!(ETEBASE_NotFoundException, "com/etebase/client/exceptions/NotFoundException"),
                    Error::Connection(_) => swig_jni_find_class!(ETEBASE_ConnectionException, "com/etebase/client/exceptions/ConnectionException"),
                    Error::TemporaryServerError(_) => swig_jni_find_class!(ETEBASE_TemporaryServerErrorException, "com/etebase/client/exceptions/TemporaryServerErrorException"),
                    Error::ServerError(_) => swig_jni_find_class!(ETEBASE_ServerErrorException, "com/etebase/client/exceptions/ServerErrorException"),
                    Error::Http(_) => swig_jni_find_class!(ETEBASE_HttpException, "com/etebase/client/exceptions/HttpException"),
                    _ => swig_jni_find_class!(ETEBASE_EtebaseException, "com/etebase/client/exceptions/EtebaseException"),
                };
                jni_throw(env, exception_class, &msg);
                return <swig_i_type!(T)>::jni_invalid_value();
            }
        };
    };
    ($p:f_type, unique_prefix="/*etebase::error::Result<swig_subst_type!(T)>*/") => "/*etebase::error::Result<swig_subst_type!(T)>*/swig_f_type!(T)"
        "swig_foreign_from_i_type!(T, $p)";
);

foreign_typemap!(
    ($p:r_type) Option<&str> => internal_aliases::JStringOptStr {
        $out = match $p {
            Some(s) => from_std_string_jstring(s.to_owned(), env),
            None => ::std::ptr::null_mut(),
        };
    };
    ($p:f_type, option = "NoNullAnnotations") => "@NonNull String" r#"
        $out = $p;
"#;
    ($p:f_type, option = "NullAnnotations") => "@Nullable String" r#"
        $out = $p;
"#;
);

#[allow(dead_code)]
fn to_java_lang_null_long(env: *mut JNIEnv, x: Option<i64>) -> internal_aliases::JLong {
    match x {
        Some(val) => {
            // Have to cache it as LONG2 because of issues with flapigen
            let class: jclass = swig_jni_find_class!(JAVA_LANG_LONG2, "java/lang/Long");
            assert!(!class.is_null(),);
            let of_m: jmethodID = swig_jni_get_static_method_id!(
                JAVA_LANG_LONG2_VALUE_OF,
                JAVA_LANG_LONG2,
                "valueOf",
                "(J)Ljava/lang/Long;"
            );
            assert!(!of_m.is_null());

            let ret = unsafe {
                let ret = (**env).CallStaticObjectMethod.unwrap()(env, class, of_m, val);
                if (**env).ExceptionCheck.unwrap()(env) != 0 {
                    panic!("valueOf failed: catch exception");
                }
                ret
            };

            assert!(!ret.is_null());
            ret
        }
        None => {
            std::ptr::null_mut()
        }
    }
}

foreign_typemap!(
    ($p:r_type) NullableI64 => internal_aliases::JLong {
        $out = to_java_lang_null_long(env, $p);
;
    };
    (f_type, option = "NoNullAnnotations") => "@NonNull Long";
    (f_type, option = "NullAnnotations") => "@Nullable Long";
);

foreign_typemap!(
    ($p:r_type) NullableString => internal_aliases::JStringOptStr {
        $out = match $p {
            Some(s) => from_std_string_jstring(s, env),
            None => ::std::ptr::null_mut(),
        };
    };
    ($p:f_type, option = "NoNullAnnotations") => "@NonNull String";
    ($p:f_type, option = "NullAnnotations") => "@Nullable String";
);

foreign_typemap!(
    ($p:r_type) CallbackOption<&str> => internal_aliases::JStringOptStr {
        $out = match $p {
            Some(s) => from_std_string_jstring(s.to_owned(), env),
            None => ::std::ptr::null_mut(),
        };
    };
);

foreign_typemap!(
    ($p:r_type) Vec<u8> => jbyteArray {
        let slice = &($p)[..];
        let slice = unsafe { std::mem::transmute::<&[u8], &[i8]>(slice) };
        let raw = JavaByteArray::from_slice_to_raw(slice, env);
        $out = raw;
    };
    ($p:f_type) => "jbyteArray";
);

foreign_typemap!(
    ($p:r_type) &'a [u8] => jbyteArray {
        let slice = unsafe { std::mem::transmute::<&[u8], &[i8]>($p) };
        let raw = JavaByteArray::from_slice_to_raw(slice, env);
        $out = raw;
    };
    ($p:f_type) => "jbyteArray";
    ($p:r_type) &'a [u8] <= jbyteArray {
        let arr = JavaByteArray::new(env, $p);
        let slice = arr.to_slice();
        let slice = unsafe { std::mem::transmute::<&[i8], &[u8]>(slice) };
        $out = slice;
    };
    ($p:f_type) <= "jbyteArray";
);

foreign_typemap!(
    ($p:r_type) Option<&'a [u8]> <= jbyteArray {
        $out = if !$p.is_null() {
            let arr = JavaByteArray::new(env, $p);
            let slice = arr.to_slice();
            let slice = unsafe { std::mem::transmute::<&[i8], &[u8]>(slice) };
            Some(slice)
        } else {
            None
        };
    };
    ($p:f_type) <= "jbyteArray";
);

#[allow(dead_code)]
fn jobject_array_to_vec_of_refs<T: SwigForeignClass>(
    env: *mut JNIEnv,
    arr: internal_aliases::JForeignObjectsArray<T>,
) -> Vec<&'static T> {
    let field_id = <T>::jni_class_pointer_field();
    assert!(!field_id.is_null());
    let length = unsafe { (**env).GetArrayLength.unwrap()(env, arr.inner) };
    let len = <usize as ::std::convert::TryFrom<jsize>>::try_from(length)
        .expect("invalid jsize, in jsize => usize conversation");
    let mut result = Vec::with_capacity(len);
    for i in 0..length {
        let native: &mut T = unsafe {
            let obj = (**env).GetObjectArrayElement.unwrap()(env, arr.inner, i);
            if (**env).ExceptionCheck.unwrap()(env) != 0 {
                panic!("Failed to retrieve element {} from this `jobjectArray'", i);
            }
            let ptr = (**env).GetLongField.unwrap()(env, obj, field_id);
            let native = (jlong_to_pointer(ptr) as *mut T).as_mut().unwrap();
            (**env).DeleteLocalRef.unwrap()(env, obj);
            native
        };
        result.push(&*native);
    }

    result
}

foreign_typemap!(
    ($p:r_type) <'a, T: SwigForeignClass> Vec<&'a T> <= internal_aliases::JForeignObjectsArray<T> {
        $out = jobject_array_to_vec_of_refs(env, $p);
    };
    ($p:f_type, option = "NoNullAnnotations") <= "swig_f_type!(T) []";
    ($p:f_type, option = "NullAnnotations") <= "@NonNull swig_f_type!(T, NoNullAnnotations) []";
);

foreign_typemap!(
    ($p:r_type) <T: SwigForeignClass> &Vec<T> => internal_aliases::JForeignObjectsArray<T> {
        $out = vec_of_objects_to_jobject_array(env, ($p).to_vec());
    };
    ($p:f_type, option = "NoNullAnnotations") => "swig_f_type!(T) []";
    ($p:f_type, option = "NullAnnotations") => "@NonNull swig_f_type!(T, NoNullAnnotations) []";
);

foreign_typemap!(
    ($p:r_type) <'a, T: SwigForeignClass> Option<Vec<&'a T>> <= internal_aliases::JForeignObjectsArray<T> {
        $out = if !$p.inner.is_null() {
            let tmp = jobject_array_to_vec_of_refs(env, $p);
            Some(tmp)
        } else {
            None
        };
    };
);

fn jstring_array_to_vec_of_strings(env: *mut JNIEnv, arr: jobjectArray) -> Vec<String> {
    let length = unsafe { (**env).GetArrayLength.unwrap()(env, arr) };
    let len = <usize as ::std::convert::TryFrom<jsize>>::try_from(length)
        .expect("invalid jsize, in jsize => usize conversation");
    let mut result = Vec::with_capacity(len);
    for i in 0..length {
        let native: String = unsafe {
            let obj: jstring = (**env).GetObjectArrayElement.unwrap()(env, arr, i);
            if (**env).ExceptionCheck.unwrap()(env) != 0 {
                panic!("Failed to retrieve element {} from this `jobjectArray'", i);
            }
            let jstr = JavaString::new(env, obj);
            jstr.to_str().to_string()
        };
        result.push(native);
    }

    result
}

#[swig_from_foreigner_hint = "java.lang.String []"]
impl SwigFrom<jobjectArray> for Vec<String> {
    fn swig_from(x: jobjectArray, env: *mut JNIEnv) -> Self {
        jstring_array_to_vec_of_strings(env, x)
    }
}
