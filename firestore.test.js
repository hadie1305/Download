const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CREATOR_UID = "creator_nioyuzi";
const READER_UID = "reader_alice";
const OTHER_READER_UID = "reader_bob";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function creatorContext() {
  return testEnv.authenticatedContext(CREATOR_UID, {
    email: "nioyuzi@gmail.com",
    email_verified: true,
  });
}

function spoofedCreatorContext() {
  return testEnv.authenticatedContext("spoof_uid", {
    email: "nioyuzi@gmail.com",
    email_verified: false,
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied across all collections", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("users").get());
  await assertFails(db.collection("novels").where("isPublished", "==", true).get());
  await assertFails(db.collection("comments").where("novelId", "==", "ryujin").get());
  await assertFails(db.collection("drive_sync_logs").get());
});

test("Email spoofing test: unverified creator email cannot access creator-only resources", async () => {
  const spoofDb = spoofedCreatorContext().firestore();
  const now = new Date();
  await assertFails(spoofDb.collection("users").get());
  await assertFails(spoofDb.collection("drive_sync_logs").get());
  await assertFails(
    spoofDb.collection("novels").doc("ryujin").set({
      authorUid: "spoof_uid",
      title: "Fake",
      authorName: "Spoof",
      genre: "Action",
      blurb: "Blurb",
      preface: "Preface",
      totalChapters: 900,
      totalArcs: 3,
      wordsPerChapter: 50000,
      isPublished: true,
      coverTag: "RYUJIN",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("User profile: reader can create/read own profile with reader role only; cannot self-promote or list all users", async () => {
  const aliceDb = testEnv.authenticatedContext(READER_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const now = new Date();

  await assertFails(
    aliceDb.collection("users").doc(READER_UID).set({
      userId: READER_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "creator",
      lastChapterRead: 1,
      wordsRead: 0,
      syncEnabled: true,
      driveLinked: false,
      driveFolderName: "",
      statusMessage: "Reading",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(READER_UID).set({
      userId: READER_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "reader",
      lastChapterRead: 1,
      wordsRead: 0,
      syncEnabled: true,
      driveLinked: false,
      driveFolderName: "",
      statusMessage: "Reading",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(READER_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Cross-user PII read rejected
  const bobDb = testEnv.authenticatedContext(OTHER_READER_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  await assertFails(bobDb.collection("users").doc(READER_UID).get());

  // Exclusive creator CAN list all users
  const creatorDb = creatorContext().firestore();
  await assertSucceeds(creatorDb.collection("users").get());
});

test("Novels & Chapters: only verified creator can create/edit; readers can read published novels", async () => {
  const creatorDb = creatorContext().firestore();
  const aliceDb = testEnv.authenticatedContext(READER_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    creatorDb.collection("novels").doc("ryujin").set({
      authorUid: CREATOR_UID,
      title: "Ryujin",
      authorName: "Nio Yuzi",
      genre: "Dark Action Thriller",
      blurb: "Kisah Kenjiro Tatsumoto meniti kegelapan demi keadilan.",
      preface: "Kata pengantar resmi penulis.",
      totalChapters: 900,
      totalArcs: 3,
      wordsPerChapter: 52500,
      isPublished: true,
      coverTag: "RYUJIN_SAGA",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reader cannot create or update novel
  await assertFails(
    aliceDb.collection("novels").doc("ryujin").update({
      title: "Hacked",
      updatedAt: new Date(),
    })
  );

  // Reader CAN list published novels
  await assertSucceeds(
    aliceDb.collection("novels").where("isPublished", "==", true).get()
  );

  // Shadow update with ghost field rejected even for creator
  await assertFails(
    creatorDb.collection("novels").doc("ryujin").update({
      ghostField: "hack",
      updatedAt: new Date(),
    })
  );
});

test("Community (comments, groups, chat_messages, status_posts) & Notifications", async () => {
  const creatorDb = creatorContext().firestore();
  const aliceDb = testEnv.authenticatedContext(READER_UID).firestore();
  const bobDb = testEnv.authenticatedContext(OTHER_READER_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("comments").doc("c_1").set({
      userId: READER_UID,
      authorName: "Alice",
      novelId: "ryujin",
      chapterNumber: 1,
      body: "Dialog pembuka Bab 1 luar biasa tegang!",
      likesCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("comments").where("novelId", "==", "ryujin").get()
  );

  // Bob cannot delete Alice's comment
  await assertFails(bobDb.collection("comments").doc("c_1").delete());

  // Reader cannot broadcast targetScope: 'all' notification
  await assertFails(
    aliceDb.collection("notifications").doc("n_1").set({
      userId: READER_UID,
      actorName: "Alice",
      title: "Fake Update",
      message: "Should fail",
      category: "chapter_update",
      targetScope: "all",
      isRead: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Creator CAN broadcast targetScope: 'all'
  await assertSucceeds(
    creatorDb.collection("notifications").doc("n_2").set({
      userId: CREATOR_UID,
      actorName: "Creator",
      title: "Bab 301 Dirilis",
      message: "Arc 2 resmi dibuka!",
      category: "chapter_update",
      targetScope: "all",
      isRead: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reader CAN query targetScope == 'all'
  await assertSucceeds(
    aliceDb.collection("notifications").where("targetScope", "==", "all").get()
  );

  // Drive sync logs restricted exclusively to creator
  await assertFails(aliceDb.collection("drive_sync_logs").get());
  await assertSucceeds(
    creatorDb.collection("drive_sync_logs").doc("log_1").set({
      creatorUid: CREATOR_UID,
      fileName: "Ryujin_Arc1_Export.pdf",
      syncType: "pdf_export",
      destination: "Google Drive / Ryujin Studio",
      status: "synced",
      wordCount: 52500,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("App distribution: only creator can publish APK/preview links; signed-in readers can read", async () => {
  const creatorDb = creatorContext().firestore();
  const aliceDb = testEnv.authenticatedContext(READER_UID).firestore();
  const now = new Date();

  await assertFails(
    aliceDb.collection("app_distribution").doc("release").set({
      updatedByUid: READER_UID,
      sharedPreviewUrl: "https://evil.example",
      apkDownloadUrl: "https://evil.example/app.apk",
      versionLabel: "1.0",
      releaseNotes: "Fake",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    creatorDb.collection("app_distribution").doc("release").set({
      updatedByUid: CREATOR_UID,
      sharedPreviewUrl: "https://ais-pre-bih67rbfpkrrba3vtfrnjq-22106480739.asia-east1.run.app",
      apkDownloadUrl: "https://drive.google.com/file/d/example/view",
      versionLabel: "v1.0.0",
      releaseNotes: "Rilis resmi Ryujin Studio 900 Bab",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("app_distribution").doc("release").get());
});
