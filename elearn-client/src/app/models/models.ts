export type Absence = {
  id: string;
  dateTime: string;
  type: AbsenceType;
  isJustified: boolean;
  student: UserInfo;
  element: Element;
};

export type AbsenceByElement = {
  element: Element;
  count: number;
  status: AbsenceStatus;
};

export enum AbsenceStatus {
  NORMAL,
  VERBAL_WARNING,
  WRITTEN_WARNING,
  DISCIPLINARY_HEARING
}

export type UserInfo = {
  id: string;
  firstName: string;
  lastName: string;
};

export enum AbsenceType {
  CLASS,
  EXAM,
  OTHER
}

export type Plan = {
  id: string;
  date: string;
  startsAt: string;
  endsAt: string;
  weeklyRepeats: number;
  type: PlanType;
  department: Department;
  year: number;
  semester: number;
  groupName: string;
  element: Element;
  room: Room;
};

export type Room = {
  id: string;
  label: string;
  floor: number;
  campus: string;
};

export type Element = {
  id: string;
  name: string;
  module: Module;
  teacher: UserInfo;
};

export type Module = {
  id: string;
  name: string;
  year: number;
  semester: number;
  department?: Department;
};

export enum PlanType {
  LECTURE = 'LECTURE',
  EXAM = 'EXAM'
}

export type PlanWithSpan = Plan & {
  rowSpan: number[];
};

export interface Result {
  id: string;
  grade: number;
  student: UserInfo;
  element: Element;
}

export type LoginRequest = {
  usernameOrEmail: string;
  password: string;
};

export type User = {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  dateOfBirth: Date;
  role: Role;
  department: Department;
  studyMode: StudyMode;
  year: number;
  semester: number;
  currentSemester: number;
  numSemesters: number;
  groupName?: string;
};

export enum StudyMode {
  HYBRID = 'HYBRID',
  ON_SITE = 'ON_SITE',
  REMOTE = 'REMOTE'
}

export enum Department {
  GENIE_INFORMATIQUE = 'GENIE_INFORMATIQUE',
  GENIE_ELECTRIQUE = 'GENIE_ELECTRIQUE',
  GENIE_CIVIL = 'GENIE_CIVIL'
}

export enum Role {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

export type UserRequest = {
  id: string;
  title: string;
  state: string;
  requestDate: Date;
  forcastDate: Date | null;
  student: UserInfo;
};

export type AbsenceCreateRequest = {
  studentId: string;
  elementId: string;
  type: AbsenceType;
  dateTime?: string;
};
