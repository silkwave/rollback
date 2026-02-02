package com.example.rollback.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.Objects;

/**
 * CtxMap: Map&lt;String,Object&gt;를 래핑하여 타입-안전 접근자를 제공하는 스레드-안전 유틸리티 클래스.
 * 실무 활용을 위해 내부적으로 ConcurrentHashMap을 사용하며, 데이터 조회 로직의 안정성을 높였습니다.
 *
 * @author Banking System Team
 * @since 2026-02-02
 */
public class CtxMap implements Serializable {

    // 직렬화 호환성을 위한 버전 UID
    private static final long serialVersionUID = 20240114L;

    /** 내부 저장소. 동시성 보장을 위해 ConcurrentHashMap 사용. */
    private final Map<String, Object> storage;

    /** 기본 생성자 (빈 ConcurrentHashMap) */
    public CtxMap() {
        this.storage = new ConcurrentHashMap<>();
    }

    /**
     * 방어적 복사 생성자.
     * 외부에서 받은 맵의 변경이 현재 맵에 영향을 주지 않도록 새로운 맵을 생성합니다.
     * 
     * @param initial 초기화에 사용할 맵. null이 아니면 모든 요소가 복사됩니다.
     */
    public CtxMap(Map<String, Object> initial) {
        this.storage = (initial != null) ? new ConcurrentHashMap<>(initial) : new ConcurrentHashMap<>();
    }

    /**
     * 정적 팩토리 메소드.
     * 
     * @param initial 초기화에 사용할 맵.
     * @return 새로운 CtxMap 인스턴스.
     */
    public static CtxMap of(Map<String, Object> initial) {
        return new CtxMap(initial);
    }

    /**
     * 비어있는 새로운 CtxMap 인스턴스를 반환합니다.
     * 
     * @return 비어있는 CtxMap 인스턴스.
     */
    public static CtxMap empty() {
        return new CtxMap();
    }

    /**
     * 키-값 쌍을 저장합니다.
     * 
     * @param key   저장할 키
     * @param value 저장할 값
     * @return 메소드 체이닝을 위한 현재 인스턴스
     */
    public CtxMap put(String key, Object value) {
        if (key != null) {
            storage.put(key, value);
        }
        return this;
    }

    /**
     * 다른 맵의 모든 데이터를 현재 맵에 병합합니다.
     * 
     * @param other 병합할 맵. null이면 아무 작업도 수행하지 않습니다.
     * @return 메소드 체이닝을 위한 현재 인스턴스
     */
    public CtxMap putAll(Map<String, ?> other) {
        if (other != null) {
            storage.putAll(other);
        }
        return this;
    }

    // --- 타입-안전 접근자 ---

    /**
     * 키와 기대 타입을 지정하여 값을 조회합니다. 타입이 일치하지 않으면 null을 반환합니다.
     *
     * @param key  키
     * @param type 기대 타입
     * @param <T>  타입 파라미터
     * @return 타입에 맞는 값 또는 null
     */
    public <T> T getObject(String key, Class<T> type) {
        Object value = storage.get(key);
        // 값의 존재 여부 및 타입 일치 여부 확인 후 안전하게 캐스팅
        return (value != null && type.isInstance(value)) ? type.cast(value) : null;
    }

    /**
     * 키로부터 Optional을 반환하는 타입-안전 조회 메서드입니다.
     * NPE(NullPointerException) 방지에 유용합니다.
     *
     * @param key  키
     * @param type 기대 타입
     * @param <T>  타입 파라미터
     * @return Optional에 감싼 값 (타입 불일치 또는 null이면 Optional.empty())
     */
    public <T> Optional<T> getOptional(String key, Class<T> type) {
        return Optional.ofNullable(getObject(key, type));
    }

    /**
     * Map<String, Object> 형태로 안전하게 조회합니다.
     * 반환되는 맵은 원본 맵의 복사본이므로, 이를 수정해도 원본 CtxMap에는 영향을 주지 않습니다.
     *
     * @param key 키
     * @return 새로운 Map<String, Object> 인스턴스 또는 null
     */
    public Map<String, Object> getMap(String key) {
        Object value = storage.get(key);
        if (value instanceof Map<?, ?> m) {
            Map<String, Object> result = new HashMap<>();
            // CtxMap의 키 타입인 String을 보장하기 위해 키 타입을 확인
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String k) {
                    result.put(k, e.getValue());
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 리스트를 타입-안전하게 조회합니다. 리스트의 요소 중 타입이 일치하지 않는 항목은 결과에서 제외됩니다.
     * 반환되는 리스트는 원본의 복사본입니다.
     *
     * @param key         키
     * @param elementType 리스트 요소의 기대 타입
     * @param <T>         요소 타입 파라미터
     * @return 타입 캐스팅된 요소들을 담은 새로운 리스트 또는 null
     */
    public <T> List<T> getList(String key, Class<T> elementType) {
        Object value = storage.get(key);
        if (value instanceof List<?> list) {
            List<T> result = new ArrayList<>();
            // 리스트의 각 요소를 순회하며 타입 안전성 검사
            for (Object o : list) {
                if (elementType.isInstance(o)) {
                    result.add(elementType.cast(o));
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 문자열을 조회합니다. 값이 문자열이 아니거나 null이면 기본값을 반환합니다.
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 문자열 또는 기본값
     */
    public String getString(String key, String defaultValue) {
        Object value = storage.get(key);
        // `toString()`을 호출하지 않고, 실제 String 타입인 경우에만 값을 반환하여 예측 가능성 높임
        if (value instanceof String s) {
            return s;
        }
        return defaultValue;
    }

    /**
     * 문자열을 조회합니다. 값이 문자열이 아니거나 null이면 빈 문자열("")을 반환합니다.
     *
     * @param key 키
     * @return 문자열 또는 빈 문자열
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * 정수를 조회합니다. 값이 Number 타입이면 int 값으로, 문자열이면 파싱하여 반환합니다.
     * 파싱 실패 시 기본값을 반환합니다.
     *
     * @param key          키
     * @param defaultValue 기본값
     * @return 정수 또는 기본값
     */
    public int getInt(String key, int defaultValue) {
        Object value = storage.get(key);
        // 1. Number 타입인 경우, 직접 int 값으로 변환 (성능상 이점)
        if (value instanceof Number n) {
            return n.intValue();
        }
        // 2. String 타입인 경우, 파싱 시도
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                // 파싱 실패 시에는 아무것도 하지 않고, 최종적으로 기본값 반환
            }
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * long 타입 정수를 조회합니다.
     * 
     * @see #getInt(String, int)
     */
    public long getLong(String key, long defaultValue) {
        Object value = storage.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                // 파싱 실패
            }
        }
        return defaultValue;
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * double 타입 실수를 조회합니다.
     * 
     * @see #getInt(String, int)
     */
    public double getDouble(String key, double defaultValue) {
        Object value = storage.get(key);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                // 파싱 실패
            }
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    /**
     * 불리언 값을 조회합니다. Boolean 타입이면 직접 반환하고,
     * 문자열이면 "true", "1", "y", "yes", "on" (대소문자 무관)을 true로 간주합니다.
     *
     * @param key 키
     * @return 불리언 값 (기본값 false)
     */
    public boolean getBoolean(String key) {
        Object value = storage.get(key);
        // 1. Boolean 타입인 경우 직접 반환
        if (value instanceof Boolean b) {
            return b;
        }
        // 2. String 타입인 경우, 널리 사용되는 '참' 표현을 확인
        if (value instanceof String s) {
            return java.util.Set.of("true", "1", "y", "yes", "on").contains(s.toLowerCase().trim());
        }
        // 그 외의 경우 모두 false
        return false;
    }

    // --- 유틸리티 메소드 ---

    /**
     * 맵에 해당 키가 존재하는지 확인합니다.
     */
    public boolean containsKey(String key) {
        return storage.containsKey(key);
    }

    /**
     * 키에 해당하는 값이 존재하는지, 그리고 문자열인 경우 내용이 비어있지 않은지 확인합니다.
     * (null이 아니고, 공백 문자열이 아님)
     *
     * @param key 확인할 키
     * @return 텍스트 내용이 있으면 true
     */
    public boolean hasText(String key) {
        Object value = storage.get(key);
        if (value instanceof String s) {
            // String.isBlank()는 공백(whitespace)만으로 이루어진 문자열도 true를 반환
            return !s.isBlank();
        }
        return value != null;
    }

    /**
     * 맵의 읽기 전용 스냅샷(복사본)을 반환합니다.
     * 이 메소드가 호출되는 시점의 맵 상태를 복사하여 반환하므로,
     * 반환된 맵은 이후의 CtxMap 변경에 영향을 받지 않습니다.
     *
     * @return 읽기 전용 맵
     */
    public Map<String, Object> asReadOnlyMap() {
        // 방어적 복사: 새로운 HashMap을 만들어 현재 상태를 복사한 후, 이를 수정 불가 맵으로 만듦
        return Collections.unmodifiableMap(new HashMap<>(storage));
    }

    /**
     * 키를 제거하고, 제거된 값을 반환합니다.
     */
    public Object remove(String key) {
        return storage.remove(key);
    }

    /**
     * 맵의 모든 요소를 제거합니다.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * 맵의 크기(엔트리 수)를 반환합니다.
     */
    public int size() {
        return storage.size();
    }

    /**
     * 맵이 비어있는지 확인합니다.
     */
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    /**
     * 키가 존재하지 않을 경우에만 값을 삽입합니다.
     */
    public Object putIfAbsent(String key, Object value) {
        return storage.putIfAbsent(key, value);
    }

    /**
     * 제공된 함수를 사용하여 값을 병합합니다.
     */
    public Object merge(String key, Object value, BiFunction<Object, Object, Object> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return storage.merge(key, value, remappingFunction);
    }

    @Override
    public String toString() {
        return "CtxMap" + storage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CtxMap ctxMap = (CtxMap) o;
        // 내부 저장소(storage)의 내용이 동일한지 비교
        return Objects.equals(storage, ctxMap.storage);
    }

    @Override
    public int hashCode() {
        // 내부 저장소(storage)를 기반으로 해시코드 생성
        return Objects.hash(storage);
    }
}