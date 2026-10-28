package lk.craftkart.pos.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SaleDao_Impl implements SaleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Sale> __insertionAdapterOfSale;

  private final EntityInsertionAdapter<SaleItem> __insertionAdapterOfSaleItem;

  public SaleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSale = new EntityInsertionAdapter<Sale>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sales` (`id`,`totalAmountCents`,`discountAmountCents`,`paymentMethod`,`notes`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Sale entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTotalAmountCents());
        statement.bindLong(3, entity.getDiscountAmountCents());
        statement.bindString(4, entity.getPaymentMethod());
        statement.bindString(5, entity.getNotes());
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfSaleItem = new EntityInsertionAdapter<SaleItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sale_items` (`id`,`saleId`,`itemId`,`itemName`,`unitPriceCents`,`quantity`,`subtotalCents`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSaleId());
        statement.bindLong(3, entity.getItemId());
        statement.bindString(4, entity.getItemName());
        statement.bindLong(5, entity.getUnitPriceCents());
        statement.bindLong(6, entity.getQuantity());
        statement.bindLong(7, entity.getSubtotalCents());
      }
    };
  }

  @Override
  public Object insert(final Sale sale, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSale.insertAndReturnId(sale);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItem(final SaleItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleItem.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Sale>> all() {
    final String _sql = "SELECT * FROM sales ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sales"}, new Callable<List<Sale>>() {
      @Override
      @NonNull
      public List<Sale> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTotalAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmountCents");
          final int _cursorIndexOfDiscountAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmountCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Sale> _result = new ArrayList<Sale>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Sale _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTotalAmountCents;
            _tmpTotalAmountCents = _cursor.getLong(_cursorIndexOfTotalAmountCents);
            final long _tmpDiscountAmountCents;
            _tmpDiscountAmountCents = _cursor.getLong(_cursorIndexOfDiscountAmountCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Sale(_tmpId,_tmpTotalAmountCents,_tmpDiscountAmountCents,_tmpPaymentMethod,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Sale> byId(final long id) {
    final String _sql = "SELECT * FROM sales WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sales"}, new Callable<Sale>() {
      @Override
      @Nullable
      public Sale call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTotalAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmountCents");
          final int _cursorIndexOfDiscountAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmountCents");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Sale _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTotalAmountCents;
            _tmpTotalAmountCents = _cursor.getLong(_cursorIndexOfTotalAmountCents);
            final long _tmpDiscountAmountCents;
            _tmpDiscountAmountCents = _cursor.getLong(_cursorIndexOfDiscountAmountCents);
            final String _tmpPaymentMethod;
            _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Sale(_tmpId,_tmpTotalAmountCents,_tmpDiscountAmountCents,_tmpPaymentMethod,_tmpNotes,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object saleWithItems(final long id,
      final Continuation<? super SaleWithItems> $completion) {
    final String _sql = "SELECT * FROM sales WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<SaleWithItems>() {
      @Override
      @Nullable
      public SaleWithItems call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTotalAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAmountCents");
            final int _cursorIndexOfDiscountAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmountCents");
            final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
            final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final LongSparseArray<ArrayList<SaleItem>> _collectionItems = new LongSparseArray<ArrayList<SaleItem>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionItems.containsKey(_tmpKey)) {
                _collectionItems.put(_tmpKey, new ArrayList<SaleItem>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipsaleItemsAslkCraftkartPosDataSaleItem(_collectionItems);
            final SaleWithItems _result;
            if (_cursor.moveToFirst()) {
              final Sale _tmpSale;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final long _tmpTotalAmountCents;
              _tmpTotalAmountCents = _cursor.getLong(_cursorIndexOfTotalAmountCents);
              final long _tmpDiscountAmountCents;
              _tmpDiscountAmountCents = _cursor.getLong(_cursorIndexOfDiscountAmountCents);
              final String _tmpPaymentMethod;
              _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
              final String _tmpNotes;
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              _tmpSale = new Sale(_tmpId,_tmpTotalAmountCents,_tmpDiscountAmountCents,_tmpPaymentMethod,_tmpNotes,_tmpCreatedAt);
              final ArrayList<SaleItem> _tmpItemsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpItemsCollection = _collectionItems.get(_tmpKey_1);
              _result = new SaleWithItems(_tmpSale,_tmpItemsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object countBetween(final long from, final long to,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sales WHERE createdAt >= ? AND createdAt < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object sumBetween(final long from, final long to,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(SUM(totalAmountCents), 0) FROM sales WHERE createdAt >= ? AND createdAt < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipsaleItemsAslkCraftkartPosDataSaleItem(
      @NonNull final LongSparseArray<ArrayList<SaleItem>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipsaleItemsAslkCraftkartPosDataSaleItem(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`saleId`,`itemId`,`itemName`,`unitPriceCents`,`quantity`,`subtotalCents` FROM `sale_items` WHERE `saleId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "saleId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfSaleId = 1;
      final int _cursorIndexOfItemId = 2;
      final int _cursorIndexOfItemName = 3;
      final int _cursorIndexOfUnitPriceCents = 4;
      final int _cursorIndexOfQuantity = 5;
      final int _cursorIndexOfSubtotalCents = 6;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<SaleItem> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final SaleItem _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpSaleId;
          _tmpSaleId = _cursor.getLong(_cursorIndexOfSaleId);
          final long _tmpItemId;
          _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
          final String _tmpItemName;
          _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
          final long _tmpUnitPriceCents;
          _tmpUnitPriceCents = _cursor.getLong(_cursorIndexOfUnitPriceCents);
          final int _tmpQuantity;
          _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
          final long _tmpSubtotalCents;
          _tmpSubtotalCents = _cursor.getLong(_cursorIndexOfSubtotalCents);
          _item_1 = new SaleItem(_tmpId,_tmpSaleId,_tmpItemId,_tmpItemName,_tmpUnitPriceCents,_tmpQuantity,_tmpSubtotalCents);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
