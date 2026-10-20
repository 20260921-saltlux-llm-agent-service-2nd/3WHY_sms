package skeleton.run;

import skeleton.controller.MemberController;

import java.util.NoSuchElementException;
import java.util.Scanner;

// 사용자 입력과 메뉴 실행을 담당한다. 이 파일은 수정하지 않는다.
public class Application {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            MemberController controller = new MemberController();
            runStartMenu(scanner, controller);
        } catch (IllegalStateException e) {
            System.out.println("데이터베이스 처리 중 오류가 발생했습니다.");
            System.out.println("db.properties 설정과 schema.sql 실행 상태를 확인하세요.");
        } catch (NoSuchElementException e) {
            System.out.println("입력이 종료되어 프로그램을 종료합니다.");
        }
    }

    private static void runStartMenu(Scanner scanner, MemberController controller) {
        while (true) {
            printStartMenu();
            int choice = readInt(scanner, "메뉴 선택: ");

            switch (choice) {
                case 1:
                    signUp(scanner, controller);
                    break;
                case 2:
                    if (login(scanner, controller)
                            && runMembershipMenu(scanner, controller)) {
                        return;
                    }
                    break;
                case 9:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("올바른 메뉴 번호를 입력하세요.");
            }
        }
    }

    private static void printStartMenu() {
        System.out.println();
        System.out.println("===== OTT 구독 관리 시스템 =====");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("9. 프로그램 종료");
    }

    private static void signUp(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 회원가입 =====");
        System.out.print("이름: ");
        String name = scanner.nextLine().trim();
        int age = readInt(scanner, "나이: ");
        System.out.print("휴대폰 번호 (예: 010-1234-5678): ");
        String phone = scanner.nextLine().trim();

        controller.signUp(name, age, phone);
    }

    private static boolean login(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 로그인 =====");
        System.out.print("휴대폰 번호 (예: 010-1234-5678): ");
        String phone = scanner.nextLine().trim();
        return controller.login(phone);
    }

    private static boolean runMembershipMenu(Scanner scanner, MemberController controller) {
        while (true) {
            printMenu();
            int choice = readInt(scanner, "메뉴 선택: ");

            switch (choice) {
                case 1:
                    controller.displayMyActiveMemberships();
                    break;
                case 2:
                    addMembership(scanner, controller);
                    break;
                case 3: {
                    if (!controller.displayMyMembershipHistory()) {
                        break;
                    }

                    int extendId = readInt(scanner, "위 목록에서 연장할 구독 ID (취소: 0): ");
                    if (extendId == 0) {
                        System.out.println("구독 연장을 취소했습니다.");
                        break;
                    }
                    int extensionDays = readInt(scanner, "연장할 일수: ");
                    controller.extendMyMembership(extendId, extensionDays);
                    break;
                }
                case 4: {
                    if (!controller.displayMyMembershipHistory()) {
                        break;
                    }

                    int deleteId = readInt(scanner, "위 목록에서 삭제할 구독 ID (취소: 0): ");
                    if (deleteId == 0) {
                        System.out.println("구독 기록 삭제를 취소했습니다.");
                        break;
                    }

                    System.out.print("정말 삭제하시겠습니까? (y/n): ");
                    String confirmation = scanner.nextLine().trim();
                    if (!"y".equalsIgnoreCase(confirmation)) {
                        System.out.println("구독 기록 삭제를 취소했습니다.");
                        break;
                    }

                    controller.deleteMyMembership(deleteId);
                    break;
                }
                case 5:
                    controller.displayMyPlanHistoryByOtt();
                    break;
                case 6:
                    System.out.print("정말 탈퇴하시겠습니까? 모든 구독 기록도 삭제됩니다. (y/n): ");
                    String confirmation = scanner.nextLine().trim();
                    if (!"y".equalsIgnoreCase(confirmation)) {
                        System.out.println("회원 탈퇴를 취소했습니다.");
                        break;
                    }

                    if (controller.deleteMyAccount()) {
                        return false;
                    }
                    break;
                case 8:
                    controller.logout();
                    return false;
                case 9:
                    System.out.println("프로그램을 종료합니다.");
                    return true;
                default:
                    System.out.println("올바른 메뉴 번호를 입력하세요.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== 내 OTT 구독 관리 =====");
        System.out.println("1. 내 현재 구독 정보 조회");
        System.out.println("2. 새로운 구독 추가");
        System.out.println("3. 구독 기간 연장");
        System.out.println("4. 구독 기록 삭제");
        System.out.println("5. OTT별 이용했던 플랜 조회");
        System.out.println("6. 회원 탈퇴");
        System.out.println("8. 로그아웃");
        System.out.println("9. 프로그램 종료");
    }

    private static void addMembership(Scanner scanner, MemberController controller) {
        System.out.println();
        System.out.println("===== 새로운 구독 추가 =====");
        System.out.print("OTT 이름 (예: NETFLIX): ");
        String ottType = scanner.nextLine().trim();
        System.out.print("플랜 이름 (예: PREMIUM): ");
        String planName = scanner.nextLine().trim();
        int subscriptionDays = readInt(scanner, "구독 일수: ");

        controller.addMyMembership(ottType, planName, subscriptionDays);
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력하세요.");
            }
        }
    }
}
