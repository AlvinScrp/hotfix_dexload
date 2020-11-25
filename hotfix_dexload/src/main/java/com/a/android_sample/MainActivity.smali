.class public Lcom/a/android_sample/MainActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "MainActivity.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 10
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .registers 4
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    .line 14
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 15
    const v1, 0x7f0a001c

    invoke-virtual {p0, v1}, Lcom/a/android_sample/MainActivity;->setContentView(I)V

    .line 16
    invoke-static {}, Lcom/a/fix/M;->a()Ljava/lang/String;

    move-result-object v0

    .line 17
    .local v0, "str":Ljava/lang/String;
    const v1, 0x7f0700af

    invoke-virtual {p0, v1}, Lcom/a/android_sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    invoke-virtual {v1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 18
    return-void
.end method
