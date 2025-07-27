#!/bin/bash

# Verification script for R8 minification setup
echo "🔍 FlipQuotes R8 Minification Verification"
echo "=========================================="

# Check build.gradle for minification
echo "1. Checking build.gradle configuration..."
if grep -q "minifyEnabled true" app/build.gradle; then
    echo "   ✅ minifyEnabled is set to true"
else
    echo "   ❌ minifyEnabled is not set to true"
    exit 1
fi

if grep -q "shrinkResources true" app/build.gradle; then
    echo "   ✅ shrinkResources is enabled"
else
    echo "   ⚠️  shrinkResources is not enabled (optional)"
fi

# Check ProGuard rules
echo ""
echo "2. Checking ProGuard rules..."
if [ -f "app/proguard-rules.pro" ]; then
    echo "   ✅ ProGuard rules file exists"
    
    if grep -q "com.app.codebuzz.flipquotes.data.Quote" app/proguard-rules.pro; then
        echo "   ✅ Quote data class is preserved"
    else
        echo "   ❌ Quote data class not preserved (Gson may fail)"
    fi
    
    if grep -q "androidx.compose" app/proguard-rules.pro; then
        echo "   ✅ Compose rules are present"
    else
        echo "   ❌ Compose rules missing (UI may break)"
    fi
    
    if grep -q "gson" app/proguard-rules.pro; then
        echo "   ✅ Gson rules are present"
    else
        echo "   ❌ Gson rules missing (JSON parsing may fail)"
    fi
else
    echo "   ❌ ProGuard rules file missing"
    exit 1
fi

# Check gitignore
echo ""
echo "3. Checking gitignore setup..."
if grep -q "mapping/" .gitignore; then
    echo "   ✅ Mapping files are ignored in git"
else
    echo "   ⚠️  Mapping files not in gitignore (they shouldn't be committed)"
fi

echo ""
echo "4. Build instructions:"
echo "   📦 Release APK: ./gradlew assembleRelease"
echo "   📱 App Bundle:  ./gradlew bundleRelease"
echo ""
echo "5. After building, find mapping file at:"
echo "   📄 app/build/outputs/mapping/release/mapping.txt"
echo ""
echo "6. Upload mapping.txt to Play Console > App content > Deobfuscation files"
echo ""
echo "✅ Setup verification complete!"
echo "💡 See DEOBFUSCATION_GUIDE.md for detailed instructions"