//package com.example.simplechatapp;
//
//import com.example.simplechatapp.dto.UserChatRoomDTO;
//import com.example.simplechatapp.entity.ActivityType;
//import com.example.simplechatapp.entity.ChatRoom;
//import com.example.simplechatapp.repository.ChatRoomRepository;
//import jakarta.persistence.EntityManager;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.extern.log4j.Log4j2;
//import org.hibernate.query.Query;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@MockitoSettings(strictness = Strictness.LENIENT)
//@Log4j2
//class ChatRoomRepositoryChatRoomTest {
//
//    @Mock
//    private EntityManager entityManager;
//
//    @Mock
//    private Query query;
//
//    @Mock
//    private ChatRoomRepository chatRoomRepository;
//
//    private static final int REPEAT_COUNT = 5;
//    private static final int TEST_DURATION_SECONDS = 30;
//    private static final String TEST_EMAIL = "test@email.com";
//
//    @BeforeEach
//    void setup() {
//        // Mock data setup
//        List<UserChatRoomDTO> mockDTOs = Collections.singletonList(
//                new UserChatRoomDTO(
//                        1L,                          // chatRoomId
//                        2L,                          // postId
//                        "Test Post Title",           // postTitle
//                        LocalDateTime.now(),         // meetingTime
//                        5,                          // participantCount
//                        10,                         // capacity
//                        LocalDateTime.now(),         // lastMessageTime
//                        "Hello World Test Message",  // lastMessagePreview
//                        ActivityType.HIKING          // activityType
//                )
//        );
//
//        // Stub the repository methods
//        when(chatRoomRepository.findUserChatRoomDTOs(anyString()))
//                .thenReturn(mockDTOs);
//        when(chatRoomRepository.findUserChatRoomDTOsNew(anyString()))
//                .thenReturn(mockDTOs);
//    }
//
//
//
//    @RepeatedTest(REPEAT_COUNT)  // 지정된 횟수(REPEAT_COUNT)만큼 테스트를 반복 실행
//    @DisplayName("기존 쿼리와 개선된 쿼리 성능 비교")  // 테스트의 목적을 설명하는 이름
//    void compareQueryPerformance(RepetitionInfo repetitionInfo) throws InterruptedException {
//        // 각 쿼리의 실행 시간을 저장할 리스트 초기화
//        List<Long> originalQueryTimes = new ArrayList<>();   // 기존 쿼리의 실행 시간을 저장할 리스트
//        List<Long> improvedQueryTimes = new ArrayList<>();   // 개선된 쿼리의 실행 시간을 저장할 리스트
//
//        // 현재 몇 번째 테스트인지 로그로 출력
//        log.info("테스트 반복 {}/{} 시작", repetitionInfo.getCurrentRepetition(), REPEAT_COUNT);
//
//        // 테스트 시작 시간 기록
//        long testStartTime = System.currentTimeMillis();
//        int iterations = 0;  // 반복 횟수를 카운트할 변수
//
//        // 지정된 테스트 시간(TEST_DURATION_SECONDS) 동안 반복
//        while (System.currentTimeMillis() - testStartTime < TEST_DURATION_SECONDS * 1000) {
//            // 기존 쿼리 테스트
//            long start = System.nanoTime();  // 시작 시간 기록
//            chatRoomRepository.findUserChatRoomDTOs(TEST_EMAIL);  // 기존 쿼리 실행
//            originalQueryTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));  // 실행 시간 저장
//
//            // 개선된 쿼리 테스트
//            start = System.nanoTime();  // 시작 시간 기록
//            chatRoomRepository.findUserChatRoomDTOsNew(TEST_EMAIL);  // 개선된 쿼리 실행
//            improvedQueryTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));  // 실행 시간 저장
//
//            iterations++;  // 반복 횟수 증가
//            Thread.sleep(100);  // 0.1초 대기 (서버 과부하 방지)
//        }
//
//        // 현재 반복의 결과 로그 출력
//        log.info("반복 {}/{} 결과:", repetitionInfo.getCurrentRepetition(), REPEAT_COUNT);
//        printPerformanceStats("기존 쿼리", originalQueryTimes, iterations);  // 기존 쿼리의 성능 통계 출력
//        printPerformanceStats("개선된 쿼리", improvedQueryTimes, iterations);  // 개선된 쿼리의 성능 통계 출력
//
//        // 테스트 결과를 TestResults 클래스에 저장
//        TestResults.addResult(repetitionInfo.getCurrentRepetition(),
//                "original", calculateStats(originalQueryTimes));  // 기존 쿼리 결과 저장
//        TestResults.addResult(repetitionInfo.getCurrentRepetition(),
//                "improved", calculateStats(improvedQueryTimes));  // 개선된 쿼리 결과 저장
//
//        // 마지막 반복일 경우 전체 결과 출력
//        if (repetitionInfo.getCurrentRepetition() == REPEAT_COUNT) {
//            printFinalResults();  // 모든 반복의 최종 결과 출력
//        }
//    }
//
//
//    @Test
//    @DisplayName("ChatRoom findByPostId 테스트")
//    void findByPostIdTest() {
//        // Given
//        Long postId = 1L;
//        ChatRoom mockChatRoom = new ChatRoom(); // ChatRoom 객체 생성
//        when(chatRoomRepository.findByPostId(postId)).thenReturn(Optional.of(mockChatRoom));
//
//        // When
//        Optional<ChatRoom> result = chatRoomRepository.findByPostId(postId);
//
//        // Then
//        assertTrue(result.isPresent());
//        verify(chatRoomRepository).findByPostId(postId);
//    }
//
//    @Test
//    @DisplayName("경계값 테스트")
//    void boundaryValueTest() {
//        // 빈 결과 테스트
//        when(chatRoomRepository.findUserChatRoomDTOsNew(TEST_EMAIL))
//                .thenReturn(Collections.emptyList());
//
//        List<UserChatRoomDTO> emptyResult =
//                chatRoomRepository.findUserChatRoomDTOsNew(TEST_EMAIL);
//        assertTrue(emptyResult.isEmpty());
//
//        // Null 값 포함 데이터 테스트
//        List<UserChatRoomDTO> nullDataResult = Collections.singletonList(
//                new UserChatRoomDTO(1L, null, null, null, 0, 0, null, null, null)
//        );
//        when(chatRoomRepository.findUserChatRoomDTOsNew(TEST_EMAIL))
//                .thenReturn(nullDataResult);
//
//        assertDoesNotThrow(() ->
//                chatRoomRepository.findUserChatRoomDTOsNew(TEST_EMAIL)
//        );
//    }
//    // Helper classes and methods
//
//    @Getter
//    @AllArgsConstructor
//    private static class PerformanceStats {
//        private final double average;
//        private final double min;
//        private final double max;
//        private final double standardDeviation;
//        private final long percentile95;
//    }
//
//    private static class TestResults {
//        private static final Map<Integer, Map<String, PerformanceStats>> results = new HashMap<>();
//        private static final Map<Integer, Map<Integer, PerformanceStats>> threadResults = new HashMap<>();
//
//        static void addResult(int repetition, String queryType, PerformanceStats stats) {
//            results.computeIfAbsent(repetition, k -> new HashMap<>())
//                    .put(queryType, stats);
//        }
//
//        static void addThreadResult(int repetition, int threadIndex, PerformanceStats stats) {
//            threadResults.computeIfAbsent(repetition, k -> new HashMap<>())
//                    .put(threadIndex, stats);
//        }
//
//        static Map<Integer, Map<String, PerformanceStats>> getResults() {
//            return results;
//        }
//
//        static Map<Integer, Map<Integer, PerformanceStats>> getThreadResults() {
//            return threadResults;
//        }
//    }
//
//    private void printPerformanceStats(String label, List<Long> times, int iterations) {
//        DoubleSummaryStatistics stats = times.stream()
//                .mapToDouble(Long::doubleValue)
//                .summaryStatistics();
//
//        log.info("{}:", label);
//        log.info("총 실행 횟수: {}", iterations);
//        log.info("평균 실행 시간: {}ms", String.format("%.2f", stats.getAverage()));
//        log.info("최소 실행 시간: {}ms", stats.getMin());
//        log.info("최대 실행 시간: {}ms", stats.getMax());
//        log.info("표준 편차: {}ms", String.format("%.2f", calculateStandardDeviation(times)));
//        log.info("95th percentile: {}ms", calculatePercentile(times, 95));
//        log.info("-------------------");
//    }
//
//    private PerformanceStats calculateStats(List<Long> times) {
//        DoubleSummaryStatistics stats = times.stream()
//                .mapToDouble(Long::doubleValue)
//                .summaryStatistics();
//        return new PerformanceStats(
//                stats.getAverage(),
//                stats.getMin(),
//                stats.getMax(),
//                calculateStandardDeviation(times),
//                calculatePercentile(times, 95)
//        );
//    }
//
//    private double calculateStandardDeviation(List<Long> times) {
//        double mean = times.stream()
//                .mapToDouble(Long::doubleValue)
//                .average()
//                .orElse(0.0);
//        return Math.sqrt(times.stream()
//                .mapToDouble(time -> Math.pow(time - mean, 2))
//                .average()
//                .orElse(0.0));
//    }
//
//    private long calculatePercentile(List<Long> times, double percentile) {
//        List<Long> sortedTimes = new ArrayList<>(times);
//        Collections.sort(sortedTimes);
//        int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
//        return sortedTimes.get(Math.max(0, index));
//    }
//
//    private void printFinalResults() {
//        Map<Integer, Map<String, PerformanceStats>> allResults = TestResults.getResults();
//
//        DoubleSummaryStatistics originalStats = allResults.values().stream()
//                .map(m -> m.get("original"))
//                .mapToDouble(PerformanceStats::getAverage)
//                .summaryStatistics();
//
//        DoubleSummaryStatistics improvedStats = allResults.values().stream()
//                .map(m -> m.get("improved"))
//                .mapToDouble(PerformanceStats::getAverage)
//                .summaryStatistics();
//
//        log.info("\n전체 테스트 결과 ({}회 반복):", REPEAT_COUNT);
//        log.info("기존 쿼리:");
//        log.info("- 평균 실행 시간: {}ms", String.format("%.2f", originalStats.getAverage()));
//        log.info("- 표준 편차: {}ms", String.format("%.2f", calculateStandardDeviation(
//                allResults.values().stream()
//                        .map(m -> m.get("original").getAverage())
//                        .mapToLong(Double::longValue)
//                        .boxed()
//                        .collect(Collectors.toList())
//        )));
//
//        log.info("개선된 쿼리:");
//        log.info("- 평균 실행 시간: {}ms", String.format("%.2f", improvedStats.getAverage()));
//        log.info("- 표준 편차: {}ms", String.format("%.2f", calculateStandardDeviation(
//                allResults.values().stream()
//                        .map(m -> m.get("improved").getAverage())
//                        .mapToLong(Double::longValue)
//                        .boxed()
//                        .collect(Collectors.toList())
//        )));
//    }
//
//
//
//
//    @Test
//    @DisplayName("findUserChatRoomDTOsNew 기본 기능 테스트")
//    void findUserChatRoomDTOsNewBasicTest() {
//        // Given
//        String testEmail = "test@email.com";
//        LocalDateTime now = LocalDateTime.now();
//
//        UserChatRoomDTO expectedDTO = new UserChatRoomDTO(
//                1L,                          // chatRoomId
//                100L,                        // postId
//                "테스트 모임",                  // postTitle
//                now.plusDays(1),             // meetingTime
//                5,                           // participantCount
//                10,                          // capacity
//                now,                         // lastMessageTime
//                "안녕하세요",                   // lastMessagePreview
//                ActivityType.HIKING          // activityType
//        );
//
//        when(chatRoomRepository.findUserChatRoomDTOsNew(testEmail))
//                .thenReturn(Collections.singletonList(expectedDTO));
//
//        // When
//        List<UserChatRoomDTO> result = chatRoomRepository.findUserChatRoomDTOsNew(testEmail);
//
//        // Then
//        assertAll(
//                () -> assertNotNull(result, "결과가 null이 아니어야 함"),
//                () -> assertEquals(1, result.size(), "결과 크기가 1이어야 함"),
//                () -> assertEquals(expectedDTO.getChatRoomId(), result.get(0).getChatRoomId()),
//                () -> assertEquals(expectedDTO.getPostTitle(), result.get(0).getPostTitle()),
//                () -> assertEquals(expectedDTO.getParticipantCount(), result.get(0).getParticipantCount()),
//                () -> assertEquals(expectedDTO.getLastMessagePreview(), result.get(0).getLastMessagePreview())
//        );
//    }
//
//    @Test
//    @DisplayName("findUserChatRoomDTOsNew 메시지 미존재 케이스 테스트")
//    void findUserChatRoomDTOsNewNoMessageTest() {
//        // Given
//        String testEmail = "test@email.com";
//        LocalDateTime now = LocalDateTime.now();
//
//        UserChatRoomDTO dtoWithoutMessage = new UserChatRoomDTO(
//                1L, 100L, "메시지 없는 채팅방",
//                now.plusDays(1), 3, 10,
//                null,  // lastMessageTime null
//                null,  // lastMessagePreview null
//                ActivityType.HIKING
//        );
//
//        when(chatRoomRepository.findUserChatRoomDTOsNew(testEmail))
//                .thenReturn(Collections.singletonList(dtoWithoutMessage));
//
//        // When
//        List<UserChatRoomDTO> result = chatRoomRepository.findUserChatRoomDTOsNew(testEmail);
//
//        // Then
//        assertAll(
//                () -> assertNotNull(result),
//                () -> assertEquals(1, result.size()),
//                () -> assertNull(result.get(0).getLastMessageTime(), "마지막 메시지 시간이 null이어야 함"),
//                () -> assertNull(result.get(0).getLastMessagePreview(), "마지막 메시지 미리보기가 null이어야 함")
//        );
//    }
//
//    @Test
//    @DisplayName("findUserChatRoomDTOsNew 메시지 길이 제한 테스트")
//    void findUserChatRoomDTOsNewMessageLengthTest() {
//        // Given
//        String testEmail = "test@email.com";
//        String longMessage = "이 메시지는 30자를 초과하는 긴 메시지입니다. 이 부분은 잘려야 합니다.";
//
//        UserChatRoomDTO dtoWithLongMessage = new UserChatRoomDTO(
//                1L, 100L, "긴 메시지 테스트",
//                LocalDateTime.now(), 5, 10,
//                LocalDateTime.now(),
//                longMessage,
//                ActivityType.HIKING
//        );
//
//        when(chatRoomRepository.findUserChatRoomDTOsNew(testEmail))
//                .thenReturn(Collections.singletonList(dtoWithLongMessage));
//
//        // When
//        List<UserChatRoomDTO> result = chatRoomRepository.findUserChatRoomDTOsNew(testEmail);
//
//        // Then
//        assertAll(
//                () -> assertNotNull(result),
//                () -> assertEquals(1, result.size()),
//                () -> assertTrue(
//                        result.get(0).getLastMessagePreview().length() <= 30,
//                        "메시지 미리보기가 30자를 초과하지 않아야 함"
//                )
//        );
//    }
//
//    @Test
//    @DisplayName("findUserChatRoomDTOsNew 다중 채팅방 정렬 테스트")
//    void findUserChatRoomDTOsNewMultipleRoomsTest() {
//        // Given
//        String testEmail = "test@email.com";
//        LocalDateTime now = LocalDateTime.now();
//
//        List<UserChatRoomDTO> multipleDTOs = Arrays.asList(
//                new UserChatRoomDTO(1L, 101L, "첫번째 채팅방",
//                        now.plusHours(1), 5, 10, now.minusMinutes(30),
//                        "최신 메시지", ActivityType.HIKING),
//
//                new UserChatRoomDTO(2L, 102L, "두번째 채팅방",
//                        now.plusHours(2), 3, 8, now,
//                        "더 최신 메시지", ActivityType.RUNNING),
//
//                new UserChatRoomDTO(3L, 103L, "세번째 채팅방",
//                        now.plusHours(3), 7, 12, now.minusHours(1),
//                        "오래된 메시지", ActivityType.CLIMBING)
//        );
//
//        when(chatRoomRepository.findUserChatRoomDTOsNew(testEmail))
//                .thenReturn(multipleDTOs);
//
//        // When
//        List<UserChatRoomDTO> result = chatRoomRepository.findUserChatRoomDTOsNew(testEmail);
//
//        // Then
//        assertAll(
//                () -> assertNotNull(result),
//                () -> assertEquals(3, result.size()),
//                () -> assertTrue(
//                        result.stream()
//                                .map(UserChatRoomDTO::getLastMessageTime)
//                                .filter(Objects::nonNull)
//                                .collect(Collectors.toList())
//                                .size() == 3,
//                        "모든 채팅방에 마지막 메시지 시간이 존재해야 함"
//                )
//        );
//
//        // 결과 로깅
//        log.info("=== 다중 채팅방 테스트 결과 ===");
//        result.forEach(dto -> log.info("""
//        채팅방: {}
//        포스트ID: {}
//        참가자: {}/{}
//        마지막 메시지: {}
//        마지막 메시지 시간: {}
//        활동 유형: {}
//        """,
//                dto.getPostTitle(),
//                dto.getPostId(),
//                dto.getParticipantCount(),
//                dto.getCapacity(),
//                dto.getLastMessagePreview(),
//                dto.getLastMessageTime(),
//                dto.getActivityType()
//        ));
//    }
//}