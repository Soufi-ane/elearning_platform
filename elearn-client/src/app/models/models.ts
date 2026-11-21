export enum RequestState {
  PENDING = 'PENDING',
  IN_REVIEW = 'IN_REVIEW',
  IN_PROGRESS = 'IN_PROGRESS',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  APPROVED = 'APPROVED',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED'
}

export type UserInfo = {
  id: string;
  firstName: string;
  lastName: string;
  role?: Role;
  department?: Department;
  year?: number;
  semester?: number;
  groupName?: string;
};

export type UserRequest = {
  id: string;
  title: string;
  state: RequestState | string;
  requestDate: string | Date;
  forcastDate: string | Date | null;
  student: UserInfo;
};

export type RequestCreatePayload = {
  title: string;
  userId?: string;
  state?: RequestState | string;
  forcastDate?: string | null;
};

export type RequestUpdatePayload = {
  title?: string;
  state?: RequestState | string;
  forcastDate?: string | null;
  userId?: string;
};

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
  NORMAL = 'NORMAL',
  VERBAL_WARNING = 'VERBAL_WARNING',
  WRITTEN_WARNING = 'WRITTEN_WARNING',
  DISCIPLINARY_HEARING = 'DISCIPLINARY_HEARING'
}

export enum AbsenceType {
  CLASS = 'CLASS',
  EXAM = 'EXAM',
  OTHER = 'OTHER'
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
  status?: 'PASSED' | 'RETAKE' | 'FAILED';
  remarks?: string | null;
  updatedAt?: string;
  student: UserBase;
  element: Element;
  gradedBy?: UserBase | null;
  lastModifiedBy?: UserBase | null;
}

export interface UserBase {
  id: string;
  firstName: string;
  lastName: string;
  role?: string;
  department?: string;
  year?: number;
  semester?: number;
  groupName?: string;
}

export interface SemesterSummary {
  semester: number;
  average: number;
  mention: string;
  totalElements: number;
  passedElements: number;
  retakeElements: number;
  failedElements: number;
  results: Result[];
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
  hasProfilePicture?: boolean;
  rootAdmin?: boolean;
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

export type AbsenceCreateRequest = {
  studentId: string;
  elementId: string;
  type: AbsenceType | string;
  dateTime?: string;
  isJustified?: boolean;
};

export type AbsenceUpdateRequest = {
  studentId?: string;
  elementId?: string;
  dateTime?: string;
  isJustified?: boolean;
  type?: AbsenceType | string;
};

export enum FileType {
  PDF = 'PDF',
  DOCX = 'DOCX',
  PPTX = 'PPTX',
  TXT = 'TXT',
  IMAGE = 'IMAGE',
  MP4 = 'MP4',
  SOURCE_CODE = 'SOURCE_CODE',
  ZIP = 'ZIP',
  OTHER = 'OTHER'
}

export type MaterialFolder = {
  id: string;
  name: string;
  parentId: string | null;
  elementId: string;
  createdAt: string;
};

export type MaterialItem = {
  id: string;
  title: string;
  description?: string | null;
  originalFileName?: string | null;
  contentType?: string | null;
  fileSize: number;
  fileType: FileType;
  assignment: boolean;
  createdAt: string;
  deadline?: string | null;
  elementId: string;
  folderId: string | null;
  uploadedBy?: UserInfo | null;
};

export type ElementWorkspace = {
  element: Element;
  canManage: boolean;
  folders: MaterialFolder[];
  materials: MaterialItem[];
};
